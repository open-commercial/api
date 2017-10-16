package sic;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import sic.controller.UnauthorizedException;
import sic.modelo.Usuario;
import sic.service.IUsuarioService;

public class JwtInterceptor extends HandlerInterceptorAdapter {
    
    @Value("${SIC_JWT_KEY}")
    private String secretkey;
    
    @Autowired
    private IUsuarioService usuarioService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
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
        Usuario usuario = usuarioService.getUsuarioPorId(idUsuario);
        if (null == usuario || null == token) {
            throw new UnauthorizedException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_token_vacio_invalido"));
        } else if (!token.equalsIgnoreCase(usuario.getToken())) {
            throw new UnauthorizedException(ResourceBundle.getBundle("Mensajes")
                    .getString("mensaje_error_token_invalido"));
        }                
        return true;
    }
}
