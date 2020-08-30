##### Spring initalizer options
   
    developer tools -all
    web - spring-web
    security - spring security
    spring-cloud-security - oauth2 security 
    testing - testcontainers

##### Create the configuration class to enable an oauth server
```
@Configuration
@EnableAuthorizationServer
public class OAuthConfig extends AuthorizationServerConfigurerAdapter {
}
```

##### run app and verify new /oauth/* endpoints are available


##### create a simple "alive" controller for testing purposes
```
@RestController
@RequestMapping(path = "/alive") 
public class AliveController {
    @GetMapping
    public String alive() {   return "alive!";   }
}
```
##### verify access to alive-controller returns "alive!"
```
curl http://localhost:8080/alive
-> returns status 200 and "alive" text
```

##### Add a Config class to require authenicated users 
```
@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
        ;
    }
```

##### verify access to alive-controller without creds fails with 401
```
curl http://localhost:8080/alive
-> returns status 401 unauthorized
```


##### verify access to alive-controller without creds fails with 401
```
# search the logs for the password with the log line begining with "Using generated security password:" 
curl -u user:380ec167-9b45-4644-952d-cb93331bb3e5 http://localhost:8080/alive
-> returns status 200 and "alive" text
```

##### replace the default security with an in-memory userdetails service 
```
@Configuration
public class ProjectConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        UserDetails john = User.withUsername("john")
                .password("12345")
                .authorities("ROLE_USER")
                .build();
        userDetailsService.createUser(john);
        return userDetailsService;
    }
```

##### override password encoder with no-op password encoder
```
  @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
```
##### test access to /alive with bad credentials
```
curl -I -u john:badpass http://localhost:8080/alive
-- returns 401
```
##### test access to /alive with good credentials
```
curl -I -u john:12345 http://localhost:8080/alive
-- returns 200
```

##### add in-memory client details service
```
public class OAuthConfig extends AuthorizationServerConfigurerAdapter {
    @Override
    public void configure(  ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.inMemory()
                .withClient("client")
                .secret("secret")
                .authorizedGrantTypes("password","authorization_code","client_credentials","refresh_token")
                .scopes("read");
    }
```

#####
```

```

##### register the authentication manager in the authorization server
```
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {
 
 
  @Autowired
  private AuthenticationManager authenticationManager;
 
  @Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints.authenticationManager(authenticationManager);
  }
```

# generate a token using  the password grant
```
curl --location -u client:secret \
--request POST 'localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read' \
--header 'Content-Type: application/json'  
``` 
you should see a response body similar to
```
{
"access_token":"47da9aac-f6f3-4ad1-a7b3-35fb7065e1e4",
"token_type":"bearer",
"refresh_token":"19004b27-1f3f-4d31-a897-7f500da06186",
"expires_in":42910,
"scope":"read"
}
```

##### enable symetric key JWT - see section 15.1.2
```
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

@Value("${jwt.key}")
  private String jwtKey;
 

@Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints
        .authenticationManager(authenticationManager)
        .tokenStore(tokenStore())
        .accessTokenConverter(
           jwtAccessTokenConverter());
  }
 
  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(
      jwtAccessTokenConverter());
  }
 
  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(jwtKey);
    return converter;
 }
```

##### add a symetric key value 
```
#application.properties
jwt.key=MjWP5L7CiD
```

##### try the password grant again to see if you get a JWT token
```
curl --location -u client:secret \
--request POST 'localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read' \
--header 'Content-Type: application/json'  
``` 
you should se JWT style tokens returned with the three dots in athe access and refresh tokens
```
{"access_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1OTg4MTEwMjgsInVzZXJfbmFtZSI6ImpvaG4iLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiYzdlZTcwMTYtYzE4OS00MTNlLTg5NjYtMzU0ZTM2Y2Y5NjZiIiwiY2xpZW50X2lkIjoiY2xpZW50Iiwic2NvcGUiOlsicmVhZCJdfQ.srE7t4IbawhlRrjOvkPnE-ZOws2a6Mj-VYTFT_vVUK4","token_type":"bearer","refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJqb2huIiwic2NvcGUiOlsicmVhZCJdLCJhdGkiOiJjN2VlNzAxNi1jMTg5LTQxM2UtODk2Ni0zNTRlMzZjZjk2NmIiLCJleHAiOjE2MDEzNTk4MjgsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJqdGkiOiI3MWRkZDRiNi0zMzI5LTRkMmUtYTEwMi0yZDhlMmY1MjYyNWQiLCJjbGllbnRfaWQiOiJjbGllbnQifQ.eYQreV9u51Xqd9o6XoTQqPY5TfvGC3pBGhB8ggXGqws","expires_in":43199,"scope":"read","jti":"c7ee7016-c189-413e-8966-354e36cf966b"}
```