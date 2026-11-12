/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.veterinaria.DTO;

import java.util.Set;

public class UsuarioDTO {

    private Long id;
    private String username;
    private String nombre;
    private String email;
    private Boolean active;
    private Set<String> roles;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id, String username, String nombre, String email, Boolean active, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.email = email;
        this.active = active;
        this.roles = roles;
    }

    // getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    
    
}