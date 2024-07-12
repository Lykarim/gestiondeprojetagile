package sn.ept.git.seminaire.cicd.cucumber.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import sn.ept.git.seminaire.cicd.entities.Tag;
import sn.ept.git.seminaire.cicd.models.TagDTO;
import sn.ept.git.seminaire.cicd.models.TodoDTO;
import sn.ept.git.seminaire.cicd.repositories.TagRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;


@Slf4j
public class CucumberStepTag {


    private final static String BASE_URI = "http://localhost";
    public static final String API_PATH = "/cicd/api/tags";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    @LocalServerPort
    private int port;
    private String name;
    private String description;
    private Response response;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TagRepository tagRepository;


    @BeforeAll
    public static void beforeAll() {
        objectMapper.findAndRegisterModules();
    }

    @Before
    public void init() {
        tagRepository.deleteAll();
    }

    protected RequestSpecification request() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
        return given()
                .contentType(ContentType.JSON)
                .log()
                .all();

    }


    @Given("acicd_tags table contains data:")
    public void tableTagContainsData(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        List<Tag> tagsList = data
                .stream()
                .map(line -> Tag
                        .builder()
                        .id(line.get(ID))
                        .name(line.get(NAME))
                        .description(line.get(DESCRIPTION))
                        .version(0)
                        .createdDate(Instant.now(Clock.systemUTC()))
                        .lastModifiedDate(Instant.now(Clock.systemUTC()))
                        .build()
                ).collect(Collectors.toList());
        tagRepository.saveAllAndFlush(tagsList);
    }

    @When("call find all tags with page = {int} and size = {int} and sort={string}")
    public void callFindAll(int page, int size, String sort) {
        response = request().contentType(ContentType.JSON)
                .log()
                .all()
                .when().get(API_PATH+String.format("?page=%d&size=%d&sort=%s",page,size,sort));
    }

    @Then("the returned http status for tag is {int}")
    public void theHttpStatusIs(int status) {
        response.then()
                .assertThat()
                .statusCode(status);
    }

    @And("the returned list of tags has {int} elements")
    public void theReturnedListHasElements(int size)  {
        Assertions.assertThat(response.jsonPath().getList("content"))
                .hasSize(size);
    }

    @And("that list of tags contains values:")
    public void thatListContainsValues(DataTable dataTable) {
        List<Map<String, String >> data =dataTable.asMaps(String.class,String.class);
        data.forEach(line-> response.then().assertThat()
                .body("content*.name", Matchers.hasItem(line.get(NAME).trim()))
                .body("content*.description", Matchers.hasItem(line.get(DESCRIPTION).trim())));
    }

    @And("that list contains tag with name={string} and description={string}")
    public void thatListContainsTagWithNameAndDescription(String name, String description) {

        if( StringUtils.isAllBlank(name,description)){
            theReturnedListHasElements(0);
            return;
        }
        response.then().assertThat()
                .body("content*.name", Matchers.hasItem(name.trim()))
                .body("content*.description", Matchers.hasItem(description.trim()));
    }

    @When("call find tag by id with id={string}")
    public void callFindTagByIdWithId(String id) {
        response = request()
                .when().get(API_PATH+"/" +id);
    }

    @And("the returned tag has properties name={string},description={string}")
    public void theReturnedTagHasPropertiesTitleDescriptionAndCompleted(String name, String description) throws JsonProcessingException {
        response.then()
                .assertThat()
                .body(NAME, CoreMatchers.equalTo(name))
                .body(DESCRIPTION, CoreMatchers.equalTo(description));
    }

    @And("tag name = {string}")
    public void name(String  name) {
        this.name=  name;
    }

    @When("call add tag")
    public void callAddTag() {
        TagDTO requestBody =TagDTO.builder().name(this.name).description(this.description).build();
        response = request()
                .body(requestBody)
                .when().post(API_PATH);
    }

    @And("tag description = {string}")
    public void description(String description) {
        this.description=description;
    }

    @And("the created tag has properties name={string}, description={string}")
    public void theCreatedTagHasPropertiesTitleAndDescriptionAndCreated_dateAndLast_modified_date(String name, String description) {
        response.then()
                .assertThat()
                .body(NAME, CoreMatchers.equalTo(name))
                .body(DESCRIPTION, CoreMatchers.equalTo(description));
    }

    @When("call update tag with id={string}")
    public void callUpdateTagWithIdAndNameAndDescription(String id) {
        TagDTO requestBody =TagDTO.builder().name(this.name).description(this.description).build();
        response = request()
                .body(requestBody)
                .when().put(API_PATH+"/"+id);
    }

    @And("the updated tag has properties name={string}, description={string}")
    public void theUpdatedTagHasPropertiesNameAndDescription(String name, String description) {
        response.then()
                .assertThat()
                .body(NAME, CoreMatchers.equalTo(name))
                .body(DESCRIPTION, CoreMatchers.equalTo(description));
    }

    @When("call delete tag with id={string}")
    public void callDeleteTagWithId(String id) {
        response = request()
                .when().delete(API_PATH+"/"+id);
    }
}

