docker run -d `
  --hostname gitlab.local `
  -p 8080:80 -p 2222:22 `
  --name gitlab `
  --restart unless-stopped `
  --memory 4g `
  -e GITLAB_ROOT_PASSWORD="root1234" `
  -e GITLAB_ROOT_EMAIL="admin@example.com" `
  -v gitlab_config:/etc/gitlab `
  -v gitlab_logs:/var/log/gitlab `
  -v gitlab_data:/var/opt/gitlab `
  gitlab/gitlab-ce:latest