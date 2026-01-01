ALTER TABLE projects
  ADD COLUMN user_id BIGINT;

-- Opcional: para ambiente já com dados, você pode definir um owner padrão
-- (recomendado só em DEV). Exemplo: seta todos para o user_id=1
-- UPDATE projects SET user_id = 1 WHERE user_id IS NULL;

ALTER TABLE projects
  ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE projects
  ADD CONSTRAINT fk_projects_user
  FOREIGN KEY (user_id) REFERENCES users(id);

CREATE INDEX idx_projects_user_id ON projects(user_id);
