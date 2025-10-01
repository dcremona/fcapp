package fcweb.backend.data.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fcweb.backend.data.Role;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "fc_attore")
public class FcAttore extends AbstractEntity{

	private String username;
	private String name;
	@JsonIgnore
	private String hashedPassword;
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Role> roles;
	@Lob
	@Column(length = 1000000)
	private byte[] profilePicture;

	@Column(name = "id_attore", nullable = false)
	private int idAttore;
	@Column(name = "desc_attore", nullable = false)
	private String descAttore;
	@Column(name = "email", nullable = false)
	private String email;
	@Column(name = "cognome", nullable = false)
	private String cognome;
	@Column(name = "nome", nullable = false)
	private String nome;
	@Column(name = "cellulare", nullable = false)
	private String cellulare;
	@Column(name = "notifiche", nullable = false)
	private boolean notifiche;
	@Column(name = "active", nullable = false)
	private boolean active;

	public int getIdAttore() {
		return idAttore;
	}

	public void setIdAttore(int idAttore) {
		this.idAttore = idAttore;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public byte[] getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(byte[] profilePicture) {
		this.profilePicture = profilePicture;
	}

	public String getDescAttore() {
		return descAttore;
	}

	public void setDescAttore(String descAttore) {
		this.descAttore = descAttore;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCellulare() {
		return cellulare;
	}

	public void setCellulare(String cellulare) {
		this.cellulare = cellulare;
	}

	public boolean isNotifiche() {
		return notifiche;
	}

	public void setNotifiche(boolean notifiche) {
		this.notifiche = notifiche;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
