#Docker Naming Conventions


### Vocabulary

* Image ID - A unique identifier for an image. Like a checksum or hash, it changes when the bits of
the image file change
* Repository - The friendly name for an image. An image like cdr-search could have many versions,
and each version would have it"s own image file with a unique Image ID, but they would all be
intances of cdr-search
* Tag - A string to identify one version from another. The default tag is "latest".
* Registry - A registry is a server of image files. Dicker assumes default registry Docker Hub
(dockerhub.io). Docker also has a convention for parsng Docker Hub usernames from a repository name.

### Docker Name Grammar
*Colons : and forward slashes / are literals*

```
name → repository:tag
repository → friendly_name | registry/friendly_name | registry/identifier/friendly_name
registry → IP:port | DNS:port
identifier → string | docker.io_username
tag → string
friendly_name → string
```
