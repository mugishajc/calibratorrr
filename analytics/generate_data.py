"""Generate fake IECMS data for the Spark lessons.

Writes three CSV files to data/raw/ (shaped like the Java app's tables):

  cases.csv        one row per court case        (big table, ~300k rows)
  hearings.csv     one row per scheduled hearing (bigger table, ~500k rows)
  court_rooms.csv  one row per court room        (tiny "dimension" table, 13 rows)

The data is random but reproducible (fixed seed), and has three properties
put there on purpose so the Spark job has something to teach:

  * SKEW:  court room KGL-01 hosts about a third of all hearings.
  * NULLS: about 0.5% of cases have no category.
  * AGE:   older cases are more likely to be closed, so there is a real backlog.

Plain Python only, no Spark needed:  python generate_data.py --cases 300000
"""
import argparse
import csv
import random
from datetime import datetime, timedelta
from pathlib import Path

AS_OF = datetime(2026, 11, 1)          # "today" for the dataset, fixed so results repeat
FIRST_DAY = datetime(2022, 1, 1)

CATEGORIES = ["CIVIL", "CRIMINAL", "COMMERCIAL", "LABOUR", "ADMINISTRATIVE"]
CATEGORY_WEIGHTS = [40, 30, 15, 10, 5]
PREFIX = {"CIVIL": "CIV", "CRIMINAL": "CRI", "COMMERCIAL": "COM",
          "LABOUR": "LAB", "ADMINISTRATIVE": "ADM", None: "UNK"}

TOPICS = {
    "CIVIL": ["Land boundary dispute", "Inheritance claim", "Unpaid rent", "Divorce petition"],
    "CRIMINAL": ["Theft charge", "Assault charge", "Fraud charge", "Traffic offence"],
    "COMMERCIAL": ["Breach of supply contract", "Unpaid invoice", "Partnership dispute"],
    "LABOUR": ["Unfair dismissal", "Unpaid wages", "Workplace injury claim"],
    "ADMINISTRATIVE": ["Permit refusal appeal", "Tax assessment appeal"],
    None: ["Unclassified matter"],
}

# (court_room, court_name, province). KGL-01 gets a much bigger weight = skew.
COURT_ROOMS = [
    ("KGL-01", "Nyarugenge Court", "Kigali City", 30),
    ("KGL-02", "Gasabo Court", "Kigali City", 6),
    ("KGL-03", "Kicukiro Court", "Kigali City", 6),
    ("NOR-01", "Musanze Court", "Northern", 5),
    ("NOR-02", "Gicumbi Court", "Northern", 5),
    ("SOU-01", "Huye Court", "Southern", 5),
    ("SOU-02", "Muhanga Court", "Southern", 5),
    ("SOU-03", "Nyanza Court", "Southern", 5),
    ("EAS-01", "Rwamagana Court", "Eastern", 5),
    ("EAS-02", "Nyagatare Court", "Eastern", 5),
    ("WES-01", "Rubavu Court", "Western", 5),
    ("WES-02", "Karongi Court", "Western", 5),
    ("WES-03", "Rusizi Court", "Western", 5),
]


def pick_status(age_days: int) -> str:
    """Older cases have travelled further along FILED -> ... -> CLOSED."""
    r = random.random()
    if r < 0.04:
        return "DISMISSED"
    progress = min(age_days / 900, 1.0) + random.uniform(-0.25, 0.25)
    steps = ["FILED", "REGISTERED", "SCHEDULED", "IN_HEARING", "JUDGED", "CLOSED"]
    return steps[max(0, min(len(steps) - 1, int(progress * len(steps))))]


def ts(d: datetime) -> str:
    return d.strftime("%Y-%m-%d %H:%M:%S")


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--cases", type=int, default=300_000)
    p.add_argument("--out", default=str(Path(__file__).parent / "data" / "raw"))
    p.add_argument("--seed", type=int, default=42)
    args = p.parse_args()

    random.seed(args.seed)
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    span_days = (AS_OF - FIRST_DAY).days
    rooms = [r[0] for r in COURT_ROOMS]
    room_weights = [r[3] for r in COURT_ROOMS]
    counters: dict = {}
    n_hearings = 0

    with open(out / "cases.csv", "w", newline="") as fc, open(out / "hearings.csv", "w", newline="") as fh:
        cw, hw = csv.writer(fc), csv.writer(fh)
        cw.writerow(["case_id", "case_number", "title", "category", "status",
                     "filed_at", "last_modified_at", "created_by"])
        hw.writerow(["hearing_id", "case_id", "scheduled_at", "court_room"])

        for case_id in range(1, args.cases + 1):
            category = None if random.random() < 0.005 else random.choices(CATEGORIES, CATEGORY_WEIGHTS)[0]
            filed = FIRST_DAY + timedelta(days=random.randrange(span_days), minutes=random.randrange(8 * 60, 17 * 60))
            age = (AS_OF - filed).days
            status = pick_status(age)
            modified = filed + timedelta(days=random.randint(0, max(age, 0)))

            key = (PREFIX[category], filed.year)
            counters[key] = counters.get(key, 0) + 1
            number = f"{key[0]}/{key[1]}/{counters[key]:05d}"

            cw.writerow([case_id, number, random.choice(TOPICS[category]), category or "", status,
                         ts(filed), ts(modified), f"registrar{random.randint(1, 5)}"])

            if status in ("SCHEDULED", "IN_HEARING", "JUDGED", "CLOSED"):
                for _ in range(random.randint(1, 4)):
                    n_hearings += 1
                    when = filed.replace(hour=0, minute=0) + timedelta(days=random.randint(14, max(age, 15)), hours=random.choice([9, 11, 14]))
                    hw.writerow([n_hearings, case_id, ts(when), random.choices(rooms, room_weights)[0]])

    with open(out / "court_rooms.csv", "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["court_room", "court_name", "province"])
        w.writerows(r[:3] for r in COURT_ROOMS)

    print(f"Wrote {args.cases:,} cases, {n_hearings:,} hearings and {len(COURT_ROOMS)} court rooms to {out}")


if __name__ == "__main__":
    main()
