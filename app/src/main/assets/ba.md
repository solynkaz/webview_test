Добавление репозитория:

```shell
curl -L https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.deb.sh | sudo bash
```

Установка runner'а (можно с указанием определенной версии):

```shell
apt-get install gitlab-runner
```

Будет создан новый пользователь **gitlab-runner**. При необходимости выдать ему права на перезапуск сервиса, создание папок, копирования файла и пр.

Регистрация runner'а (подробно описано в **[документации](https://docs.gitlab.com/runner/register/index.html)**):

```shell
sudo gitlab-runner register

# Теперь вводим адрес, на котором располагается хост GitLab
Please enter the gitlab-ci coordinator URL (e.g. https://example.com )
https://gitlab.sibdigital.net

# Далее токен runner'а, который можно получить по https://example.com/admin/runners для shared runner'ов, для каждого проекта можно настроить свой, токен можно найти в репозитории
Please enter the gitlab-ci token for this runner
xxx

# Далее вводим описание runner (или hostname)
Please enter the gitlab-ci description for this runner
[hostame] test-docker

# Теги
Please enter the gitlab-ci tags for this runner (comma separated):
test-server test-docker

# Надо ли GitLab выполнять не тегированные работы (например, можем захотеть чтобы выполнял с тегами rails)
Whether to run untagged jobs [true/false]:
[false]: false

# Привязать ли runner к проекту?
Whether to lock Runner to current project [true/false]:
[true]: true

# Выбор executor (для Pages выберем docker, почему не docker с machine уже описывалось)
Please enter the executor: ssh, docker+machine, docker-ssh+machine, kubernetes, docker, parallels, virtualbox, docker-ssh, shell:
docker

# Дефолтный Image если в .gitlab-ci.yml в репозитории не будет указан Image
Please enter the Docker image (eg. ruby:2.1):
alpine:latest
```

Для использования данного runner'а в CI/CD надо в скрипте `gitlab-ci.yml` в нужном этапе в разделе `tags` указать `test-docker`.

Наиболее часто используемые типы:

- `docker` - загружает и запускает образ Docker и внутри него выполняет инструкции
- `shell` - обычный shell. Инструкции будут выполняться под пользователем **gitlab-runner**, необходимо это учитывать

Для типа раннера `docker` добавить в конфигурацию следующее:

```
volumes = ['/var/run/docker.sock:/var/run/docker.sock', '/cache']
```
