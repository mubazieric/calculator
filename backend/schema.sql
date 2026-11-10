CREATE TABLE IF NOT EXISTS ai_usage (
  token_hash text NOT NULL,
  month_utc text NOT NULL,
  used integer NOT NULL DEFAULT 0 CHECK (used >= 0),
  PRIMARY KEY (token_hash, month_utc)
);
CREATE TABLE IF NOT EXISTS ai_requests (
  token_hash text NOT NULL,
  request_id uuid NOT NULL,
  month_utc text NOT NULL,
  status text NOT NULL CHECK (status IN ('pending','complete')),
  result jsonb,
  input_tokens integer,
  output_tokens integer,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (token_hash, request_id)
);
CREATE INDEX IF NOT EXISTS ai_requests_pending_idx ON ai_requests (token_hash, month_utc, created_at) WHERE status = 'pending';
