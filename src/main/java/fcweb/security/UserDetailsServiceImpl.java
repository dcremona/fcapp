package fcweb.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.service.AttoreRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

	private final AttoreRepository userRepository;

	public UserDetailsServiceImpl(AttoreRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		FcAttore user = userRepository.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("No user present with username: " + username);
		} else {
			return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getHashedPassword(),getAuthorities(user));
		}
	}

	private static List<GrantedAuthority> getAuthorities(FcAttore user) {
		return user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toList());

	}

}
