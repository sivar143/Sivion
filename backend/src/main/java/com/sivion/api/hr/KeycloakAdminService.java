package com.sivion.api.hr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.util.*;

@Service
public class KeycloakAdminService {
  private final RestClient client;
  private final String adminUrl, adminUser, adminPassword, realm;
  private volatile String accessToken;
  private volatile Instant tokenExpiresAt = Instant.EPOCH;

  public KeycloakAdminService(
      @Value("${sivion.keycloak.admin-base-url:http://localhost:8081}") String adminUrl,
      @Value("${sivion.keycloak.admin-username:admin}") String adminUser,
      @Value("${sivion.keycloak.admin-password:admin}") String adminPassword,
      @Value("${sivion.keycloak.realm:sivion}") String realm) {
    this.adminUrl = adminUrl.replaceAll("/+$", "");
    this.adminUser = adminUser;
    this.adminPassword = adminPassword;
    this.realm = realm;
    this.client = RestClient.create();
  }

  public String createUser(String username, String email, String firstName, String lastName,
                           String password, boolean temporary, String role, boolean enabled) {
    requireText(username, "Username");
    requireText(password, "Password");
    Map<String,Object> body = new LinkedHashMap<>();
    body.put("username", username.trim());
    body.put("email", email);
    body.put("firstName", firstName);
    body.put("lastName", lastName);
    body.put("enabled", enabled);
    body.put("emailVerified", false);
    body.put("credentials", List.of(Map.of("type","password","value",password,"temporary",temporary)));

    ResponseEntity<Void> response = admin().post()
        .uri(adminUrl + "/admin/realms/" + realm + "/users")
        .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();

    String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
    String userId = location == null ? findUserId(username) : location.substring(location.lastIndexOf('/') + 1);
    if (userId == null || userId.isBlank()) throw new IllegalStateException("Keycloak created the user but did not return its id");
    setRealmRole(userId, role);
    return userId;
  }

  public void updateUser(String userId, String username, String email, String firstName, String lastName,
                         Boolean enabled, String password, Boolean temporary, String role) {
    if (userId == null || userId.isBlank()) throw new IllegalArgumentException("Keycloak user id is required");
    Map<String,Object> body = new LinkedHashMap<>();
    body.put("username", username);
    body.put("email", email);
    body.put("firstName", firstName);
    body.put("lastName", lastName);
    if (enabled != null) body.put("enabled", enabled);
    admin().put().uri(adminUrl + "/admin/realms/" + realm + "/users/" + userId)
        .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();

    if (password != null && !password.isBlank()) {
      Map<String,Object> credential = Map.of("type","password","value",password,"temporary",Boolean.TRUE.equals(temporary));
      admin().put().uri(adminUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password")
          .contentType(MediaType.APPLICATION_JSON).body(credential).retrieve().toBodilessEntity();
    }
    if (role != null && !role.isBlank()) setRealmRole(userId, role);
  }

  public void deleteUser(String userId) {
    if (userId == null || userId.isBlank()) return;
    admin().delete().uri(adminUrl + "/admin/realms/" + realm + "/users/" + userId).retrieve().toBodilessEntity();
  }

  private void setRealmRole(String userId, String role) {
    if (role == null || role.isBlank()) return;
    String path = adminUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
    List<Map<String,Object>> current = admin().get().uri(path).retrieve()
        .body(new ParameterizedTypeReference<List<Map<String,Object>>>() {});
    List<Map<String,Object>> managed = current == null ? List.of() :
        current.stream().filter(x -> x.get("name") != null && isManagedRole(x.get("name").toString())).toList();
    if (!managed.isEmpty()) {
      admin().method(HttpMethod.DELETE).uri(path).contentType(MediaType.APPLICATION_JSON).body(managed).retrieve().toBodilessEntity();
    }
    Map<String,Object> representation = admin().get()
        .uri(adminUrl + "/admin/realms/" + realm + "/roles/" + role).retrieve()
        .body(new ParameterizedTypeReference<Map<String,Object>>() {});
    if (representation == null) throw new IllegalArgumentException("Keycloak role not found: " + role);
    admin().post().uri(path).contentType(MediaType.APPLICATION_JSON)
        .body(List.of(representation)).retrieve().toBodilessEntity();
  }

  private boolean isManagedRole(String role) {
    return Set.of("ADMIN","HR_ADMIN","HR_USER","MANAGER","EMPLOYEE","SALES_MANAGER","SALES_USER",
        "INVENTORY_MANAGER","INVENTORY_USER","PROCUREMENT_MANAGER","FINANCE_MANAGER","FINANCE_USER","MARKETING_USER")
        .contains(role);
  }

  private String findUserId(String username) {
    List<Map<String,Object>> users = admin().get()
        .uri(builder -> builder.scheme(java.net.URI.create(adminUrl).getScheme())
            .host(java.net.URI.create(adminUrl).getHost())
            .port(java.net.URI.create(adminUrl).getPort())
            .path("/admin/realms/" + realm + "/users").queryParam("username", username).build())
        .retrieve().body(new ParameterizedTypeReference<List<Map<String,Object>>>() {});
    if (users == null || users.isEmpty()) return null;
    return Objects.toString(users.get(0).get("id"), null);
  }

  private RestClient admin() {
    if (accessToken == null || Instant.now().isAfter(tokenExpiresAt.minusSeconds(20))) refreshToken();
    return RestClient.builder().defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).build();
  }

  private synchronized void refreshToken() {
    if (accessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(20))) return;
    MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
    form.add("grant_type","password"); form.add("client_id","admin-cli");
    form.add("username",adminUser); form.add("password",adminPassword);
    Map<String,Object> token = client.post()
        .uri(adminUrl + "/realms/master/protocol/openid-connect/token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve()
        .body(new ParameterizedTypeReference<Map<String,Object>>() {});
    accessToken = Objects.toString(token == null ? null : token.get("access_token"), null);
    Number expires = token == null ? null : (Number) token.get("expires_in");
    if (accessToken == null) throw new IllegalStateException("Unable to authenticate to Keycloak administration API");
    tokenExpiresAt = Instant.now().plusSeconds(expires == null ? 60 : expires.longValue());
  }

  private static void requireText(String value, String field) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
  }
}
