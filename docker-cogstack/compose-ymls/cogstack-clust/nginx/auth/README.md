Basic Auth with nginx
=====================
https://docs.nginx.com/nginx/admin-guide/security-controls/configuring-http-basic-authentication/

Password file creation utility such as apache2-utils

```sh
$ sudo htpasswd -c ./auth/.htpasswd user1
```

Press Enter and type the password for user1 at the prompts.

Create additional user-password pairs. Omit the -c flag because the file already exists:

```sh
$ sudo htpasswd -c ./auth/.htpasswd user1
```

You can confirm that the file contains paired usernames and encrypted passwords:

```sh
$ cat ./auth/.htpasswd

user1:$apr1$/woC1jnP$KAh0SsVn5qeSMjTtn0E9Q0
user2:$apr1$QdR8fNLT$vbCEEzDj7LyqCMyNpSoBh/
user3:$apr1$Mr5A0e.U$0j39Hp5FfxRkneklXaMrr/
```


