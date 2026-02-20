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



##컨테이너 확인/시작
#docker ps --filter name=gitlab
#docker start gitlab

#콘솔 접속 (비밀번호가 없기에)
# docker exec -it gitlab gitlab-rails console

#소속 생성하기
#org = Organizations::Organization.first
#org ||= Organizations::Organization.create!(name: 'Default', path: 'default')

#root생성하기.. 추후 정보변경할것
#user = User.new(
##username: 'root',
##email: 'admin@example.com',
##name: 'Administrator',
##password: 'aB3$xY9#kL2mN5pQ',
##password_confirmation: 'aB3$xY9#kL2mN5pQ',
##admin: true
#)
#user.skip_confirmation!
#user.build_namespace(path: 'root', name: 'Administrator', organization: org)
#user.save!
