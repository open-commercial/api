package sic.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sic.modelo.Credencial;
import sic.modelo.Usuario;
import sic.service.IUsuarioService;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    
    private final IUsuarioService usuarioService;
        
    @Value("${SIC_JWT_KEY}")
    private String secretkey;
    
    @Autowired
    public AuthController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
    private String generarToken(long idUsuario) {
        // 24hs desde la fecha actual para expiration
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();
        return Jwts.builder()
                   .setIssuedAt(today)
                   .setExpiration(tomorrow)
                   .signWith(SignatureAlgorithm.HS512, secretkey)
                   .claim("idUsuario", idUsuario)
                   .compact();
    }
    
    @PostMapping("/login")
    public String login(@RequestBody Credencial credencial) {
        Usuario usuario;
        try {
            usuario = usuarioService.autenticarUsuario(credencial);
        } catch (EntityNotFoundException ex) {
            throw new UnauthorizedException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_usuario_logInInvalido"), ex);
        }
        String token = this.generarToken(usuario.getId_Usuario());
        usuarioService.actualizarToken(token, usuario.getId_Usuario());        
        return token;
    }
    
    @PutMapping("/logout")
    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_token_vacio_invalido"));
        }
        final String token = authHeader.substring(7); // The part after "Bearer "
        Claims claims;
        try {
            claims = Jwts.parser()
                         .setSigningKey(secretkey)
                         .parseClaimsJws(token)
                         .getBody();
            request.setAttribute("claims", claims);
        } catch (JwtException ex) {
            throw new UnauthorizedException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_token_vacio_invalido"), ex);
        }
        long idUsuario = (int) claims.get("idUsuario");        
        usuarioService.actualizarToken("", idUsuario);
    }
    
}
