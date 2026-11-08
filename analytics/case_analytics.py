"""IECMS court-case analytics with Apache Spark (Day 4 lab).

Each lesson below matches a part of the Day 4 slides and prints what to look
at. Run the whole thing, then read the code of one lesson at a time.

  Lesson 1  SparkSession, explicit schema, partitions        (slides 13, 17, 42)
  Lesson 2  Lazy evaluation: transformations vs actions      (slides 16, 25, 28)
  Lesson 3  Same question three ways: RDD, DataFrame, SQL    (slides 48, 49)
  Lesson 4  Narrow vs wide: where the shuffle appears        (slides 27, 31, 51)
  Lesson 5  Caching: pay once, reuse many times              (slides 32, 61)
  Lesson 6  Broadcast join with a small table                (slides 56, 57)
  Lesson 7  Data skew and salting                            (slide 59)
  Lesson 8  Nulls: count(*) vs count(col)                    (slide 45)
  Lesson 9  Window functions: the case backlog               (slide 43)
  Lesson 10 Writing partitioned Parquet, predicate pushdown  (slides 54, 55, 85)

Run:  ./run.sh            (or: python case_analytics.py --pause  to keep the Spark UI open)
"""
import argparse
import os
import sys
import time
from operator import add
from pathlib import Path

from pyspark.sql import SparkSession, Window
from pyspark.sql import functions as F
from pyspark.sql.types import LongType, StringType, StructField, StructType, TimestampType

HERE = Path(__file__).parent
OPEN_STATUSES = ["FILED", "REGISTERED", "SCHEDULED", "IN_HEARING"]

# Explicit schemas (slide 42): faster than inferSchema (no extra pass over the
# file) and the types never change by surprise.
CASE_SCHEMA = StructType([
    StructField("case_id", LongType(), False),
    StructField("case_number", StringType()),
    StructField("title", StringType()),
    StructField("category", StringType()),
    StructField("status", StringType()),
    StructField("filed_at", TimestampType()),
    StructField("last_modified_at", TimestampType()),
    StructField("created_by", StringType()),
])
HEARING_SCHEMA = StructType([
    StructField("hearing_id", LongType(), False),
    StructField("case_id", LongType()),
    StructField("scheduled_at", TimestampType()),
    StructField("court_room", StringType()),
])
ROOM_SCHEMA = StructType([
    StructField("court_room", StringType(), False),
    StructField("court_name", StringType()),
    StructField("province", StringType()),
])


def lesson(n: int, title: str) -> None:
    print(f"\n{'=' * 78}\nLESSON {n}: {title}\n{'=' * 78}")


def timed(label: str, fn):
    start = time.perf_counter()
    result = fn()
    print(f"  {label}: {(time.perf_counter() - start) * 1000:,.0f} ms")
    return result


def read_csv(spark, name, schema):
    return (spark.read
            .option("header", True)
            .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
            .schema(schema)
            .csv(str(HERE / "data" / "raw" / name)))


# ---------------------------------------------------------------------------
def lesson_1_session(spark, cases):
    lesson(1, "SparkSession, schema and partitions")
    print(f"  master        : {spark.sparkContext.master}   (local[*] = one JVM, one thread per core)")
    print(f"  Spark UI      : {spark.sparkContext.uiWebUrl}   (open it while the job runs)")
    print(f"  cases schema  : {cases.schema.simpleString()[:90]}...")
    print(f"  partitions    : {cases.rdd.getNumPartitions()}  -> that many tasks per stage when reading")
    print("  Each partition is processed by one task on one core. That is Spark's unit of parallelism.")


def lesson_2_lazy(cases):
    lesson(2, "Lazy evaluation")
    open_cases = timed("build filter+select plan (transformations)",
                       lambda: cases.filter(F.col("status").isin(OPEN_STATUSES)).select("case_id", "category"))
    print("  -> only milliseconds: Spark recorded the plan but did not read the file.")
    n = timed("count() (an action, so a job runs)", open_cases.count)
    print(f"  -> {n:,} open cases. The file was read just now, triggered by the action.")
    timed("count() again with no cache", open_cases.count)
    print("  -> the second action re-ran the whole plan, reading the CSV again (see Lesson 5).")


def lesson_3_three_ways(spark, cases):
    lesson(3, "Same question three ways: cases filed per category in 2025")

    # (a) RDD: lines of text, we parse them ourselves; Spark sees opaque objects.
    lines = spark.sparkContext.textFile(str(HERE / "data" / "raw" / "cases.csv"))
    header = lines.first()
    rdd_result = (lines.filter(lambda l: l != header)
                  .map(lambda l: l.split(","))                       # narrow
                  .filter(lambda r: r[5].startswith("2025") and r[3])  # narrow
                  .map(lambda r: (r[3], 1))                          # narrow
                  .reduceByKey(add)                                  # wide (shuffle), with map-side combine
                  .collect())                                        # action
    rdd = dict(rdd_result)

    # (b) DataFrame: declarative; Catalyst sees columns and can optimise.
    df = (cases.filter(F.year("filed_at") == 2025)
          .filter(F.col("category").isNotNull())
          .groupBy("category").count())
    df_result = {r["category"]: r["count"] for r in df.collect()}

    # (c) SQL: same engine and same optimiser as (b), only the language differs.
    cases.createOrReplaceTempView("cases")
    sql = spark.sql("""
        SELECT category, COUNT(*) AS count
        FROM cases
        WHERE year(filed_at) = 2025 AND category IS NOT NULL
        GROUP BY category
    """)
    sql_result = {r["category"]: r["count"] for r in sql.collect()}

    assert rdd == df_result == sql_result, "the three answers should match"
    for cat in sorted(rdd, key=rdd.get, reverse=True):
        print(f"  {cat:<15} {rdd[cat]:>7,}")
    print("  RDD, DataFrame and SQL all agree. The DataFrame and SQL versions build the same optimised")
    print("  plan; the RDD version is longer and Catalyst cannot optimise it.")


def lesson_4_narrow_wide(cases):
    lesson(4, "Narrow vs wide transformations (read the physical plans)")
    print("  Narrow only (filter + select): no 'Exchange' in the plan, so no shuffle, one stage:")
    cases.filter(F.col("status") == "FILED").select("case_number").explain()
    print("  Wide (groupBy): look for 'Exchange hashpartitioning(status...)'. That is the shuffle and")
    print("  the stage boundary. Note the partial HashAggregate BEFORE it (the map-side combine,")
    print("  like reduceByKey):")
    cases.groupBy("status").count().explain()


def lesson_5_cache(cases):
    lesson(5, "Caching")
    open_cases = cases.filter(F.col("status").isin(OPEN_STATUSES)).cache()   # lazy: nothing stored yet
    timed("1st count (reads CSV, fills the cache)", open_cases.count)
    timed("2nd count (served from memory)", open_cases.count)
    print("  The Storage tab in the Spark UI now shows the cached DataFrame.")
    print("  Three reports reuse the cache, so the CSV is not read again:")
    by_status = open_cases.groupBy("status").count().orderBy("status")
    by_category = open_cases.groupBy("category").count().orderBy(F.desc("count"))
    by_registrar = open_cases.groupBy("created_by").count().orderBy("created_by")
    for name, df in [("by status", by_status), ("by category", by_category), ("by registrar", by_registrar)]:
        print(f"  open cases {name}: " + ", ".join(f"{r[0]}={r[1]:,}" for r in df.collect()))
    open_cases.unpersist()
    print("  unpersist() frees the memory. Cache only data you reuse (slide 20, misconception 4).")


def lesson_6_broadcast(hearings, rooms):
    lesson(6, "Broadcast join: big hearings table x tiny court_rooms table")
    joined = hearings.join(F.broadcast(rooms), "court_room")
    print("  Look for 'BroadcastHashJoin' in the plan: the 13-row table is copied to every executor,")
    print("  so the large hearings table is never shuffled:")
    joined.explain()
    per_province = (joined.groupBy("province", F.year("scheduled_at").alias("year"))
                    .count().orderBy("year", "province"))
    print("  Hearings per province per year:")
    per_province.filter(F.col("year") >= 2025).show(truncate=False)


def lesson_7_skew(hearings):
    lesson(7, "Data skew and salting")
    print("  Hearings per court room (KGL-01 is the hot key):")
    hearings.groupBy("court_room").count().orderBy(F.desc("count")).show(5)

    def partition_sizes(df):
        rows = df.groupBy(F.spark_partition_id().alias("p")).count().collect()
        return sorted((r["count"] for r in rows), reverse=True)

    skewed = partition_sizes(hearings.repartition(8, "court_room"))
    print(f"  repartition(8, court_room)        -> rows per partition: {skewed}")
    print(f"     biggest / smallest = {skewed[0] / skewed[-1]:.1f}x. One task does most of the work (slide 64).")

    salted = hearings.withColumn("salt", (F.rand(seed=1) * 8).cast("int"))
    balanced = partition_sizes(salted.repartition(8, "court_room", "salt"))
    print(f"  repartition(8, court_room, salt)  -> rows per partition: {balanced}")
    print(f"     biggest / smallest = {balanced[0] / balanced[-1]:.1f}x. The hot key is spread over all tasks.")
    print("  Aggregate in two steps after salting: first by (key, salt), then by key.")
    two_step = (salted.groupBy("court_room", "salt").count()
                .groupBy("court_room").agg(F.sum("count").alias("hearings"))
                .orderBy(F.desc("hearings")))
    top = two_step.first()
    print(f"     two-step result for the hot key: {top['court_room']} = {top['hearings']:,} hearings (same answer)")


def lesson_8_nulls(cases):
    lesson(8, "Nulls")
    r = cases.agg(F.count("*").alias("rows"), F.count("category").alias("with_category")).first()
    print(f"  count(*) = {r['rows']:,}    count(category) = {r['with_category']:,}")
    print(f"  -> {r['rows'] - r['with_category']:,} cases have a NULL category. count(col) skips nulls.")
    print("  category = NULL is never true (comparing with NULL gives 'unknown'); use isNull():")
    equals_null = cases.filter(F.col("category") == None).count()  # noqa: E711  (wrong on purpose)
    print(f"     filter(category == None) -> {equals_null} rows")
    print(f"     filter(isNull())         -> {cases.filter(F.col('category').isNull()).count()} rows")
    print("  For reports, fill them: cases.fillna({'category': 'UNKNOWN'})")


def lesson_9_backlog(cases, as_of):
    lesson(9, f"Window functions: open-case backlog as of {as_of}")
    backlog = (cases.fillna({"category": "UNKNOWN"})
               .filter(F.col("status").isin(OPEN_STATUSES))
               .withColumn("age_days", F.datediff(F.lit(as_of), F.col("filed_at"))))
    summary = (backlog.groupBy("category")
               .agg(F.count("*").alias("open_cases"),
                    F.round(F.avg("age_days")).cast("int").alias("avg_age_days"),
                    F.sum(F.when(F.col("age_days") > 365, 1).otherwise(0)).alias("older_than_1y"))
               .orderBy(F.desc("open_cases")))
    summary.show()

    oldest_first = Window.partitionBy("category").orderBy(F.col("filed_at"))
    print("  The 2 oldest open cases in each category (row_number over a window):")
    (backlog.withColumn("rank", F.row_number().over(oldest_first))
     .filter("rank <= 2")
     .select("category", "rank", "case_number", "status", "age_days")
     .orderBy("category", "rank")
     .show(12, truncate=False))
    return backlog, summary


def lesson_10_write_and_pushdown(spark, backlog, summary):
    lesson(10, "Writing partitioned Parquet and reading it back with pushdown")
    out = HERE / "data" / "curated"
    # repartition("category") first so each category folder gets ONE file, not one per task
    # (avoids the small-files problem, slide 85).
    (backlog.repartition("category")
     .write.mode("overwrite").partitionBy("category")
     .parquet(str(out / "backlog")))
    # A small summary for a dashboard: coalesce(1) gives a single CSV file, no full shuffle.
    summary.coalesce(1).write.mode("overwrite").option("header", True).csv(str(out / "backlog_summary"))
    folders = sorted(p.name for p in (out / "backlog").iterdir() if p.is_dir())
    print(f"  wrote {out / 'backlog'} with one folder per category: {folders}")
    print(f"  wrote {out / 'backlog_summary'} (one CSV for BI tools)")

    civil_old = (spark.read.parquet(str(out / "backlog"))
                 .filter((F.col("category") == "CIVIL") & (F.col("age_days") > 365))
                 .select("case_number", "age_days"))
    print("\n  Reading back only CIVIL cases older than a year. In the plan look for:")
    print("    PartitionFilters: [... category = CIVIL]  -> other category folders are skipped entirely")
    print("    PushedFilters: [... GreaterThan(age_days,365)] -> Parquet skips row groups using min/max stats")
    print("    ReadSchema: only case_number and age_days -> projection pruning (columnar format)")
    civil_old.explain()
    print(f"  CIVIL cases open for more than a year: {civil_old.count():,}")


# ---------------------------------------------------------------------------
def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--as-of", default="2026-11-01", help="date used to compute case age")
    p.add_argument("--pause", action="store_true", help="wait for Enter at the end so you can browse the Spark UI")
    args = p.parse_args()

    if not (HERE / "data" / "raw" / "cases.csv").exists():
        raise SystemExit("No data yet. Run:  python generate_data.py")

    # RDD code (lambdas) runs in separate Python worker processes. Make them use this same
    # Python (the venv), not whatever "python3" is first on the PATH.
    os.environ["PYSPARK_PYTHON"] = sys.executable
    os.environ["PYSPARK_DRIVER_PYTHON"] = sys.executable
    # Everything stays on this laptop: bind to localhost, so the UI is http://localhost:4040.
    os.environ.setdefault("SPARK_LOCAL_IP", "127.0.0.1")

    spark = (SparkSession.builder
             .appName("iecms-case-analytics")
             .master("local[*]")
             .config("spark.driver.host", "localhost")
             .config("spark.driver.bindAddress", "127.0.0.1")
             # The default of 200 shuffle partitions is far too many for one laptop: tiny tasks,
             # mostly overhead (slide 65, over-partitioning). 8 suits a local run.
             .config("spark.sql.shuffle.partitions", "8")
             .getOrCreate())
    spark.sparkContext.setLogLevel("WARN")

    cases = read_csv(spark, "cases.csv", CASE_SCHEMA)
    hearings = read_csv(spark, "hearings.csv", HEARING_SCHEMA)
    rooms = read_csv(spark, "court_rooms.csv", ROOM_SCHEMA)

    lesson_1_session(spark, cases)
    lesson_2_lazy(cases)
    lesson_3_three_ways(spark, cases)
    lesson_4_narrow_wide(cases)
    lesson_5_cache(cases)
    lesson_6_broadcast(hearings, rooms)
    lesson_7_skew(hearings)
    lesson_8_nulls(cases)
    backlog, summary = lesson_9_backlog(cases, args.as_of)
    lesson_10_write_and_pushdown(spark, backlog, summary)

    if args.pause:
        print(f"\nAll lessons done. Spark UI: {spark.sparkContext.uiWebUrl}  (Jobs, Stages, Storage, SQL tabs)")
        try:
            input("Press Enter (or Ctrl+C) to stop Spark.\n")
        except EOFError:                       # started without a terminal: wait until killed
            try:
                while True:
                    time.sleep(3600)
            except KeyboardInterrupt:
                pass
        except KeyboardInterrupt:
            pass
    spark.stop()


if __name__ == "__main__":
    main()
