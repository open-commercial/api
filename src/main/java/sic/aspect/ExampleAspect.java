package sic.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class ExampleAspect {

  @Before("@annotation(logExecutionTime)")
  public void logExecutionTime(LogExecutionTime logExecutionTime) {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    String token = request.getHeader("Authorization");
    System.out.println(logExecutionTime.value());
  }
}
