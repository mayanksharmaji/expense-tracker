import os
from flask import Flask, request, render_template, redirect, url_for
import sqlite3
from datetime import datetime, date, timedelta, timezone

app = Flask(__name__)

def init_db():
    conn = sqlite3.connect('expenses.db')
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS expenses (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            amount REAL NOT NULL,
            category TEXT NOT NULL,
            note TEXT,
            date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS plan (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            pocket_money REAL NOT NULL,
            savings_goal REAL NOT NULL,
            cycle_length INTEGER NOT NULL,
            start_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

init_db()

def get_db():
    conn = sqlite3.connect('expenses.db')
    conn.row_factory = sqlite3.Row
    return conn

def today_utc():
    return datetime.now(timezone.utc).date()

def parse_date(date_str):
    if date_str:
        try:
            return datetime.strptime(date_str[:10], '%Y-%m-%d').date()
        except:
            pass
    return None

CATEGORY_EMOJIS = {
    'Food': '🍔',
    'Travel': '🚌',
    'Shopping': '🛍',
    'Bills': '💡',
    'Medical': '💊',
    'Entertainment': '🎮',
    'Education': '📚',
    'Others': '📦'
}

@app.route('/')
def home():
    conn = get_db()
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM plan ORDER BY id DESC LIMIT 1")
    plan = cursor.fetchone()

    cursor.execute("SELECT * FROM expenses ORDER BY date DESC")
    expenses = cursor.fetchall()

    conn.close()

    spending_budget = 0
    daily_allowance = 0
    total_spent = 0
    remaining_budget = 0
    cycle_ended = False
    category_stats = {}
    today_spent = 0
    health = None

    if plan:
        spending_budget = plan['pocket_money'] - plan['savings_goal']
        daily_allowance = round(spending_budget / plan['cycle_length'], 2)

        today_str = today_utc().strftime('%Y-%m-%d')

        for expense in expenses:
            total_spent += expense['amount']
            cat = expense['category']
            category_stats[cat] = category_stats.get(cat, 0) + expense['amount']
            if expense['date'] and expense['date'][:10] == today_str:
                today_spent += expense['amount']

        remaining_budget = round(spending_budget - total_spent, 2)

        if daily_allowance > 0:
            diff = daily_allowance - today_spent
            if diff >= daily_allowance * 0.2:
                health = ('🟢 Excellent', f'₹{diff:.0f} under budget', 'green')
            elif diff >= 0:
                health = ('🟡 Careful', 'Almost reached daily limit', 'yellow')
            else:
                health = ('🔴 Overspent', f'₹{abs(diff):.0f} over today\'s limit', 'red')

        start_d = parse_date(plan['start_date'])
        if start_d:
            elapsed = (today_utc() - start_d).days
            if elapsed >= plan['cycle_length']:
                cycle_ended = True

    recent_expenses = expenses[:5] if expenses else []
    sorted_categories = sorted(category_stats.items(), key=lambda x: x[1], reverse=True)

    return render_template(
        'home.html',
        plan=plan,
        spending_budget=spending_budget,
        daily_allowance=daily_allowance,
        total_spent=total_spent,
        remaining_budget=remaining_budget,
        cycle_ended=cycle_ended,
        recent_expenses=recent_expenses,
        category_emojis=CATEGORY_EMOJIS,
        category_stats=sorted_categories,
        today_spent=today_spent,
        health=health
    )

@app.route('/add', methods=['POST'])
def add_expense():
    amount = request.form['amount']
    category = request.form['category']
    note = request.form.get('note', '')

    conn = sqlite3.connect('expenses.db')
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO expenses (amount, category, note) VALUES (?, ?, ?)",
        (amount, category, note)
    )
    conn.commit()
    conn.close()

    return redirect(url_for('home'))

@app.route('/save_plan', methods=['POST'])
def save_plan():
    pocket_money = request.form['pocket_money']
    savings_goal = request.form['savings_goal']
    cycle_length = request.form['cycle_length']

    conn = sqlite3.connect('expenses.db')
    cursor = conn.cursor()
    cursor.execute("DELETE FROM plan")
    cursor.execute(
        "INSERT INTO plan (pocket_money, savings_goal, cycle_length) VALUES (?, ?, ?)",
        (pocket_money, savings_goal, cycle_length)
    )
    conn.commit()
    conn.close()

    return redirect(url_for('home'))

@app.route('/delete_plan', methods=['POST'])
def delete_plan():
    conn = sqlite3.connect('expenses.db')
    cursor = conn.cursor()
    cursor.execute("DELETE FROM plan")
    conn.commit()
    conn.close()
    return redirect(url_for('home'))

@app.route('/delete/<int:expense_id>', methods=['POST'])
def delete_expense(expense_id):
    conn = sqlite3.connect('expenses.db')
    cursor = conn.cursor()
    cursor.execute("DELETE FROM expenses WHERE id = ?", (expense_id,))
    conn.commit()
    conn.close()
    return redirect(url_for('home'))

def relative_date_str(d):
    today = today_utc()
    if d == today:
        return "Today"
    if d == today - timedelta(days=1):
        return "Yesterday"
    return d.strftime('%d %B')

@app.route('/history')
def history():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM expenses ORDER BY date DESC")
    expenses = cursor.fetchall()
    conn.close()

    # Enrich expenses with relative date labels and group by date
    grouped = []
    current_date_val = None
    current_group = None

    for e in expenses:
        d = parse_date(e['date'])
        label = relative_date_str(d) if d else 'Unknown'
        key = d.strftime('%Y-%m-%d') if d else 'unknown'

        enriched = dict(e)
        enriched['time'] = e['date'][11:16] if e['date'] else ''
        enriched['date_label'] = label
        enriched['date_key'] = key

        if key != current_date_val:
            if current_group is not None:
                grouped.append((current_date_label, current_group))
            current_date_val = key
            current_date_label = label
            current_group = []
        current_group.append(enriched)

    if current_group is not None:
        grouped.append((current_date_label, current_group))

    total = sum(e['amount'] for e in expenses)

    return render_template(
        'history.html',
        grouped=grouped,
        total=total,
        category_emojis=CATEGORY_EMOJIS
    )

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=True)