package com.jnj.saml.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import org.junit.Test;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.*;

import static org.junit.Assert.*;


public class SamlGeneratorServiceTest {

    SamlGeneratorService samlGeneratorService = new SamlGeneratorService();

    @Test
    public void generatesValidAssertionId(){
        //String assertionId = "_d71a3a8e9fcc45c9e9d248ef7049393fc8f04e5f75";
        assertThat(isValidAssertionId(samlGeneratorService.generateAssertionId())).isTrue();
    }

    private boolean isValidAssertionId(String assertionId){
        System.out.println(assertionId);
        Pattern pattern = Pattern.compile(samlGeneratorService.ASSERTION_ID_REGEX);
        Matcher matcher = pattern.matcher(assertionId);
        return matcher.matches();
    }
}
