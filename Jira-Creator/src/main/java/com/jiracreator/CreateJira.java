package com.jiracreator;

import org.apache.log4j.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.LogManager;

import javax.naming.AuthenticationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.yamlbeans.YamlException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

public class CreateJira {
    //static Logger logger = Logger.getLogger(CreateJira.class);
    private static String base_url = "http://localhost:8080/rest/api/2/issue";
    public static Logger logger1 = null;
    private static final Logger LOGGER = Logger.getLogger( CreateJira.class.getName() );
    public static void main(String[] args)
            throws FileNotFoundException, YamlException, NoSuchFileException, IOException, ParseException {
        //logger.debug("This is debug message");
        //logger.info("This is info message");
        //logger.warn("This is warn message");
       // logger.fatal("This is fatal message");
       // logger.error("This is error message");
       // LogManager lgmngr = LogManager.getLogManager();
        Logger logger = Logger.getLogger(CreateJira.class.getName());
        LOGGER.info("Logging an INFO-level message");
        try {
            
            String content = "";
            String auth = "";

            InputStream input = CreateJira.class.getClassLoader().getResourceAsStream("config.properties");
            Properties prop = new Properties();
            prop.load(input);
            logger.info(" jira server for creating an issue");

            Options options = new Options();
            logger.info("accepting cmd input");
            Option uname = new Option("un", "uname", true, "input username ");
            uname.setRequired(true);
            options.addOption(uname);

            Option pwd = new Option("p", "pwd", true, "input password ");
            pwd.setRequired(true);
            options.addOption(pwd);

            Option url = new Option("url", "url", true, "input url");
            url.setRequired(true);
            options.addOption(url);

            Option projName = new Option("pj", "projName", true, "input project name ");
            projName.setRequired(true);
            options.addOption(projName);

            if (args.length == 0) {
                logger.info("when args are not given");

                base_url = prop.getProperty("jira_server");
                String u_name = prop.getProperty("username").toString();

                String pass = prop.getProperty("password").toString();

                String combine = u_name + ":" + pass;

                auth = new String(Base64.encode(combine));

                content = new String(Files.readAllBytes(Paths.get(
                        "C:\\Users\\Jira-Creator\\Jira-Creator\\src\\main\\resources\\jira_template.yml")));
                String yamlFileValues = convertYamlToJson(content);
                String jsonValues = yamlFileValues.toString();

                logger.info(jsonValues);

                String issue = postMethod(auth, base_url, jsonValues);
                logger.info(issue);
                JSONObject issueObj = new JSONObject(issue);
                String newKey = issueObj.getString("key");
                logger.info("Key:" + newKey);
            }

            if (args.length > 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("utility-name", options);
                CommandLineParser parser = new DefaultParser();

                CommandLine cmd = null;
                cmd = parser.parse(options, args);
               // formatter.printHelp("utility-name", options);
                String username = cmd.getOptionValue("uname");
                String password = cmd.getOptionValue("pwd");
                String jira_url = cmd.getOptionValue("url");
                String proj_name = cmd.getOptionValue("projName");

                base_url = jira_url.toString();
                String u_name1 = username.toString();

                String pass1 = password.toString();

                String combine1 = u_name1 + ":" + pass1;

                auth = new String(Base64.encode(combine1));
                //System.out.println(auth);

                content = new String(Files.readAllBytes(Paths.get(
                        "C:\\Users\\Jira-Creator\\Jira-Creator\\src\\main\\resources\\jira_template.yml")));
                String yamlFileValues = convertYamlToJson(content);
                String jsonValues = yamlFileValues.toString();

                logger.info(jsonValues);

                String issue = postMethod(auth, base_url, jsonValues);
                logger.info(issue);
                JSONObject issueObj = new JSONObject(issue);
                String newKey = issueObj.getString("key");
                logger.info("Key:" + newKey);

            }

            System.exit(1);

        }

        catch (ParseException e) {
            logger.info(e.getMessage());

        }

        catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            logger.info("Username or Password wrong");

        } catch (JSONException e) {
            logger.info("Invalid JSON output");

        } catch (ClientHandlerException e) {
            logger.info("Error invoking REST method");

        }

    }

    private static String postMethod(String auth, String url, String data)
            throws AuthenticationException, ClientHandlerException {
        Client client = Client.create();
        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.header("Authorization", "Basic " + auth).type("application/json")
                .accept("application/json").post(ClientResponse.class, data);
        int statusCode = response.getStatus();
        if (statusCode == 401) {
            throw new AuthenticationException("Invalid Username or Password");
        }
        return response.getEntity(String.class);
    }

    private static String convertYamlToJson(String yaml) {
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(yaml, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}