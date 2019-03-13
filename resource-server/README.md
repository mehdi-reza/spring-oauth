##### 1. Copy public.key file

Make sure you have copied public.key file from authorisation-server src/main/resources/public.key in to this projects src/main/resources folder

`curl -v -H 'Authorization: bearer <access_token>' http://localhost:8090/echo`

#### Caution

This implementation is based on JWT token signer where oauth-server-jwt.jks file is extremely important. If this keystore file is leaked then your system can be compromised.

The resource server only takes care if the access token is signed by the authorization server certificate, and it trusts the token including the principal and all granted roles.
