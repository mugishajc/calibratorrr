# IECMS analytics with Apache Spark (Day 4 lab)

A small PySpark job that analyses court cases and hearings shaped like the IECMS app's tables. It is written as ten short lessons, each matching part of the Day 4 slides (MIT 91205, Apache Spark). Every lesson prints a real result and tells you what to look for.

The Java app stores a few cases in memory; analytics needs volume. So `generate_data.py` creates realistic fake data:

| File | Rows | Role in the lessons |
|---|---|---|
| `cases.csv` | 300,000 | the main fact table |
| `hearings.csv` | ~600,000 | the big side of a join; one court room (KGL-01) is a hot key, to show skew |
| `court_rooms.csv` | 13 | the small side of a join, to show a broadcast join |

About 0.5% of cases have no category, to show null handling.

## Run it

You need Python 3.12 and Java 17 (Spark 4 runs on Java 17 or 21). On this Mac both are already installed.

```bash
cd analytics
./run.sh            # first run creates .venv, installs PySpark, generates the data
./run.sh --pause    # same, but waits at the end so you can explore the Spark UI
```

Or step by step:

```bash
cd analytics
python3.12 -m venv .venv
.venv/bin/pip install -r requirements.txt
.venv/bin/python generate_data.py                 # --cases 1000000 for a bigger run
JAVA_HOME=$(/usr/libexec/java_home -v 17) .venv/bin/python case_analytics.py
```

The whole run takes well under a minute on a laptop. Output is written to `data/curated/` (ignored by git).

## The lessons

| # | Lesson | What you see | Slides |
|---|---|---|---|
| 1 | SparkSession and partitions | master, Spark UI address, number of partitions = number of tasks | 13, 17, 23 |
| 2 | Lazy evaluation | building a plan takes ~0 ms, `count()` runs a job, a 2nd `count()` reruns everything | 16, 25, 28 |
| 3 | Same question three ways | cases per category in 2025 with RDD `reduceByKey`, DataFrame, and SQL, all giving the same answer | 48, 49 |
| 4 | Narrow vs wide | `explain()` with no `Exchange` (narrow) vs `Exchange hashpartitioning` (shuffle) | 27, 31, 51 |
| 5 | Caching | second count served from memory; three reports reuse the cache; `unpersist()` | 32, 61 |
| 6 | Broadcast join | `BroadcastHashJoin` in the plan; hearings per province per year | 56, 57 |
| 7 | Skew and salting | rows per partition before and after salting the hot key | 59, 64 |
| 8 | Nulls | `count(*)` vs `count(col)`; why `== None` matches nothing | 45 |
| 9 | Window functions | backlog per category; the oldest open cases with `row_number()` | 43 |
| 10 | Writing and pushdown | partitioned Parquet, one file per folder; `PartitionFilters`, `PushedFilters`, pruned `ReadSchema` | 54, 55, 85 |

## What to look at in the Spark UI

Run with `--pause` and open the address printed in Lesson 1 (usually http://localhost:4040).

- **Jobs:** one job per action (`count`, `collect`, `show`, `write`). Lesson 2's two counts are two jobs.
- **Stages:** a job splits into stages at each shuffle. Click a stage from Lesson 7 to see one task far slower than the rest (skew).
- **Storage:** the cached DataFrame from Lesson 5, with its size in memory.
- **SQL / DataFrame:** the plan graph for each query. Find the `Exchange` (shuffle) and `BroadcastExchange` boxes.

## Exercises

Try these after one full run. Each matches a Knowledge Review question from the slides.

1. **(Q3, Q9)** In Lesson 3, replace `reduceByKey(add)` with `groupByKey().mapValues(len)`. The answer is the same. Compare "Shuffle Write" for that stage in the Spark UI and explain the difference.
2. **(Q8)** In Lesson 6, remove `F.broadcast(...)` and set `spark.sql.autoBroadcastJoinThreshold` to `-1` in `main()`. What join does `explain()` show now, and how many extra shuffles appear?
3. **(Q11)** Generate 1,000,000 cases (`--cases 1000000`) and rerun Lesson 7. Does the skew ratio change? Why does salting help a `groupBy` but need extra work for a join?
4. **(Q4)** Change `spark.sql.shuffle.partitions` to `200`. Count the tasks in a shuffle stage and time the run. Then try `2`. Which runs fastest on your laptop, and why?
5. **(Q12)** Add a report: average number of hearings per case, per category. It needs a join of `cases` and `hearings`, two large tables. Read the plan: which join strategy did Spark choose?
6. **(Q13, Q14)** The whole dataset is about 60 MB. Would Spark be the right tool for this in production? What would change your answer?

## Files

| File | What it is |
|---|---|
| `generate_data.py` | plain Python, writes the CSVs to `data/raw/` |
| `case_analytics.py` | the Spark job; one function per lesson |
| `run.sh` | sets Java 17, creates the venv, generates data if missing, runs the job |
| `requirements.txt` | the pinned PySpark version |
