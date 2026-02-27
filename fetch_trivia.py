#!/usr/bin/env python3
"""
fetch_trivia.py — Fetch geography questions from Open Trivia DB
and convert them into the Firebase Realtime Database format used by GeoSphere.

Usage:
    pip install requests
    python3 fetch_trivia.py

Output:
    trivia_questions.json  (ready to import into Firebase via Import JSON)

Firebase structure produced:
    questions/
      world/  q1…qN
      europe/ q1…qN
      ...

Open Trivia DB category IDs:
    22 = Geography
    23 = History
    19 = Mathematics  (not used here)

API docs: https://opentdb.com/api_config.php
"""

import html
import json
import random
import time
import urllib.request


# ── Configuration ──────────────────────────────────────────────────
# How many questions to fetch per difficulty per category
QUESTIONS_PER_BATCH = 50   # max 50 per API call
DIFFICULTY_LEVELS   = ["easy", "medium", "hard"]

# GeoSphere category tags → Open Trivia DB category ID
CATEGORY_MAP = {
    "world":     22,    # Geography
    "europe":    22,
    "asia":      22,
    "americas":  22,
    "africa":    22,
    "capitals":  22,
    "flags":     22,
    "landmarks": 22,
}

BASE_URL = "https://opentdb.com/api.php"


def fetch_questions(category_id: int, difficulty: str, amount: int = 50) -> list:
    url = (
        f"{BASE_URL}"
        f"?amount={amount}"
        f"&category={category_id}"
        f"&difficulty={difficulty}"
        f"&type=multiple"
    )
    try:
        with urllib.request.urlopen(url, timeout=10) as resp:
            data = json.loads(resp.read())
        if data.get("response_code") == 0:
            return data.get("results", [])
        print(f"  API response_code={data.get('response_code')} for {difficulty}")
    except Exception as e:
        print(f"  Error fetching: {e}")
    return []


def convert_question(item: dict, index: int) -> tuple[str, dict]:
    """Convert an OpenTDB question dict to Firebase format."""
    question_text = html.unescape(item["question"])
    correct       = html.unescape(item["correct_answer"])
    incorrect     = [html.unescape(a) for a in item["incorrect_answers"]]
    difficulty    = item.get("difficulty", "medium")
    points        = {"easy": 1, "medium": 1, "hard": 2}.get(difficulty, 1)

    # Build shuffled options list
    all_options = incorrect + [correct]
    random.shuffle(all_options)

    correct_index = all_options.index(correct)

    # Options stored as a Map with string keys "0"–"3" (required by FirebaseHelper)
    options = {str(i): opt for i, opt in enumerate(all_options)}

    key = f"q{index:04d}"
    entry = {
        "questionText":       question_text,
        "options":            options,
        "correctOptionIndex": correct_index,
        "explanation":        f"The correct answer is: {correct}",
        "difficulty":         difficulty,
        "points":             points,
    }
    return key, entry


def main():
    print("🌍 GeoSphere — Open Trivia DB Fetcher")
    print("=" * 50)
    print(f"Fetching {QUESTIONS_PER_BATCH} questions × {len(DIFFICULTY_LEVELS)} difficulties")
    print(f"= up to {QUESTIONS_PER_BATCH * len(DIFFICULTY_LEVELS)} raw questions")
    print()

    all_questions: list[dict] = []

    for difficulty in DIFFICULTY_LEVELS:
        print(f"[{difficulty.upper()}] Fetching from Open Trivia DB…")
        questions = fetch_questions(
            category_id=22,  # Geography
            difficulty=difficulty,
            amount=QUESTIONS_PER_BATCH
        )
        print(f"  → Got {len(questions)} questions")
        all_questions.extend(questions)
        time.sleep(1)   # Be polite to the free API

    print(f"\nTotal fetched: {len(all_questions)} questions")
    print("Distributing across GeoSphere categories…\n")

    # Shuffle and distribute across categories
    random.shuffle(all_questions)

    categories = list(CATEGORY_MAP.keys())
    buckets: dict[str, list] = {cat: [] for cat in categories}

    # Round-robin distribution
    for i, q in enumerate(all_questions):
        buckets[categories[i % len(categories)]].append(q)

    firebase_data: dict = {"questions": {}}
    total = 0

    for cat, items in buckets.items():
        firebase_data["questions"][cat] = {}
        for idx, item in enumerate(items, start=1):
            key, entry = convert_question(item, idx)
            firebase_data["questions"][cat][key] = entry
        count = len(items)
        total += count
        print(f"  {cat}: {count} questions")

    output_file = "trivia_questions.json"
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(firebase_data, f, indent=2, ensure_ascii=False)

    print(f"\n✅ Done! {total} questions written to '{output_file}'")
    print()
    print("Next steps:")
    print("  1. Go to Firebase Console → Realtime Database")
    print("  2. Click ⋮ → Import JSON")
    print(f"  3. Upload '{output_file}'")
    print()
    print("💡 Tip: Run this script multiple times and MERGE the JSONs to grow your question bank.")
    print("        Each run fetches fresh questions. Max 150 per run (API limit).")


if __name__ == "__main__":
    main()
