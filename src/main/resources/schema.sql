CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name     TEXT NOT NULL,
    email         TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role          TEXT NOT NULL CHECK (role IN ('STUDENT', 'ADMIN')),
    created_at    TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reports (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL REFERENCES users(id),
    type        TEXT NOT NULL CHECK (type IN ('LOST', 'FOUND')),
    title       TEXT NOT NULL,
    category    TEXT NOT NULL,
    description TEXT,
    zone        TEXT NOT NULL,
    event_date  TEXT NOT NULL,
    photo_path  TEXT,
    status      TEXT NOT NULL DEFAULT 'OPEN'
                CHECK (status IN ('OPEN', 'MATCHED', 'CLAIMED',
                                  'RETURNED', 'CLOSED')),
    created_at  TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS matches (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    lost_report_id  INTEGER NOT NULL REFERENCES reports(id),
    found_report_id INTEGER NOT NULL REFERENCES reports(id),
    text_score      REAL NOT NULL,
    image_score     REAL,
    final_score     REAL NOT NULL,
    status          TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'CONFIRMED',
                                      'REJECTED')),
    created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (lost_report_id, found_report_id)
);

CREATE TABLE IF NOT EXISTS claims (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    match_id           INTEGER NOT NULL REFERENCES matches(id),
    claimant_id        INTEGER NOT NULL REFERENCES users(id),
    hidden_detail_text TEXT NOT NULL,
    status             TEXT NOT NULL DEFAULT 'SUBMITTED'
                       CHECK (status IN ('SUBMITTED', 'APPROVED',
                                         'REJECTED')),
    pickup_code        TEXT,
    created_at         TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS handovers (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    claim_id   INTEGER NOT NULL UNIQUE REFERENCES claims(id),
    admin_id   INTEGER NOT NULL REFERENCES users(id),
    handed_at  TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id    INTEGER NOT NULL REFERENCES users(id),
    message    TEXT NOT NULL,
    is_read    INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
