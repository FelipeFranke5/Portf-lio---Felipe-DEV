CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    stack TEXT[] NOT NULL DEFAULT '{}',
    github_url VARCHAR(255),
    demo_url VARCHAR(255),
    featured BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS skills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    level SMALLINT CHECK (level BETWEEN 1 AND 5)
);

CREATE TABLE IF NOT EXISTS contact_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sent BOOLEAN NOT NULL DEFAULT false,
    retry_count SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS internal_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    simple_class_name VARCHAR(255) NOT NULL,
    error_message VARCHAR(400) NOT NULL,
    stack_trace TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);