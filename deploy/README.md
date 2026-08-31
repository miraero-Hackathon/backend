# EC2 Tomcat deployment

The WAR contains only environment-variable placeholders. Runtime secrets stay on EC2.

## 1. Runtime environment

Create `/etc/miraero/miraero.env` from `.env.example` and restrict access:

```bash
sudo install -d -m 750 -o root -g tomcat /etc/miraero
sudo install -m 640 -o root -g tomcat .env.example /etc/miraero/miraero.env
sudo vi /etc/miraero/miraero.env
```

Do not add `export` in this file. Quote values that contain whitespace.
Local development and EC2 use the same property keys. EC2 values override the local defaults through this environment file.

## 2. Tomcat service

Install the systemd override and restart Tomcat:

```bash
sudo install -d /etc/systemd/system/tomcat9.service.d
sudo install -m 644 deploy/tomcat/miraero.conf /etc/systemd/system/tomcat9.service.d/miraero.conf
sudo systemctl daemon-reload
sudo systemctl restart tomcat9
sudo systemctl status tomcat9
```

The service name, group, and webapps directory may differ when Tomcat was installed manually.

## 3. GitHub Actions secrets

Configure `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, and `EC2_KNOWN_HOSTS` in the GitHub repository. The EC2 user needs passwordless sudo permission only for the deployment commands used by the workflow.
