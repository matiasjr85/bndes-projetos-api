CREATE INDEX IF NOT EXISTS ix_projects_search
ON projects
USING GIN (to_tsvector('portuguese', coalesce(name,'') || ' ' || coalesce(description,'')));