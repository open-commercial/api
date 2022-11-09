package sic.config;

import org.javers.spring.auditable.AuthorProvider;

public class MockAuthorProvider implements AuthorProvider {


    @Override
    public String provide() {
        return "testUsuario";
    }
}