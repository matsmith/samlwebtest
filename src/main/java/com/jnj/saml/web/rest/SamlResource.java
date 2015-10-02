package com.jnj.saml.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.jnj.saml.domain.Authority;
import com.jnj.saml.domain.PersistentToken;
import com.jnj.saml.domain.User;
import com.jnj.saml.repository.PersistentTokenRepository;
import com.jnj.saml.repository.UserRepository;
import com.jnj.saml.security.SecurityUtils;
import com.jnj.saml.service.MailService;
import com.jnj.saml.service.SamlGeneratorService;
import com.jnj.saml.service.UserService;
import com.jnj.saml.web.rest.dto.UserDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.ssl.Base64;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.ResponseMarshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/saml")
public class SamlResource {

    private final Logger log = LoggerFactory.getLogger(SamlResource.class);

    @Inject
    private SamlGeneratorService samlGeneratorService;

    /**
     * POST  /register -> register the user.
     */
    @RequestMapping(value = "/createresponse",
        method = RequestMethod.POST,
        produces = "text/html" )
    @Timed
    public String createSamlResponse(@RequestParam(value = "customerId") String customerId, @RequestParam(value = "resourceUrl") String resourceUrl,  HttpServletRequest request) throws MarshallingException {
    //public String createSamlResponse(HttpServletRequest request) throws MarshallingException {
        samlGeneratorService.setCustomerId(customerId);
        samlGeneratorService.setResourceUrl(resourceUrl);
        DateTime now = new DateTime();
        Assertion assertion = samlGeneratorService.generateSamlResponse(samlGeneratorService.populateAssertionData(), now);
        samlGeneratorService.signAssertion(assertion);
        Response samlResponse = samlGeneratorService.createSamlResponse(now, assertion);

        ResponseMarshaller responseMarshaller = new ResponseMarshaller();
        Element samlResponseElement = responseMarshaller.marshall(samlResponse);
        String samlResponsePlainText = XMLHelper.nodeToString(samlResponseElement);
       // log.info("SAML Response as String: " + samlResponsePlainText);


        byte[]  encodedSamlResponse = Base64.encodeBase64(samlResponsePlainText.getBytes());
        log.info("encoded value is " + new String(encodedSamlResponse));

        return new String(encodedSamlResponse);
        //return samlResponsePlainText;
    }

    /**
     * GET  /activate -> activate the registered user.
     */
    @RequestMapping(value = "/samldump",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> dumpSamlResponse(@RequestParam(value = "SAMLResponse") String samlResponse, @RequestParam(value = "RelayState") String relayState) {

        return new ResponseEntity<>(HttpStatus.CREATED);
    }



    /**
     * GET  /activate -> activate the registered user.
     */
    @RequestMapping(value = "/test",
        method = RequestMethod.POST,
        produces = "text/html" )
    @Timed
    public ModelAndView testsaml(HttpServletRequest request, HttpServletResponse response) {

        //return "redirect:http://www.google.com";
        //return new ModelAndView("forward:https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/sp-localhost-coaching");
        //response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        return new ModelAndView("redirect:https://localhost-coaching.healthmedia.net/saml/module.php/saml/sp/saml2-acs.php/sp-localhost-coaching");

    }
}
