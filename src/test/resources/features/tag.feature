

Feature: API to manage Tag Items

  #find all
  #find by id
  #add
  #update
  #delete

  Scenario: Find all tags should return correct list
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When call find all tags with page = 0 and size = 10 and sort="name,asc"
    Then the returned http status for tag is 200
    And the returned list of tags has 2 elements
    And that list of tags contains values:
      | name   | description   |
      | tag 1 | description 1 |
      | tag 2 | description 2 |


  Scenario Outline: Find all tags with pageable should return correct list
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When call find all tags with page = <page> and size = <size> and sort="name,asc"
    Then the returned http status for tag is 200
    And the returned list of tags has <returned_list_size> elements
    And that list contains tag with name="<name>" and description="<description>"
    Examples:
      |page| size  | returned_list_size | name   | description   |
      |0   | 1     |  1                 | tag 1 | description 1 |
      |1   | 1     |  1                 | tag 2 | description 2 |
      |1   | 3     |  0                 |         |               |
      |2   | 1     |  0                 |         |               |

  Scenario Outline: Find all tags with sorting should return correct list
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When call find all tags with page = <page> and size = <size> and sort="<sort>"
    Then the returned http status for tag is 200
    And the returned list of tags has <returned_list_size> elements
    And that list contains tag with name="<name>" and description="<description>"
    Examples:
      |page| size  | sort        | returned_list_size | name   | description   |
      |0   | 1     | name,asc    |         1          | tag 1 | description 1 |
      |1   | 1     | name,asc   |         1          | tag 2 | description 2 |

  Scenario Outline: Find tag by id should return correct list
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When call find tag by id with id="<id>"
    Then the returned http status for tag is 200
    And the returned tag has properties name="<name>",description="<description>"
    Examples:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|

  Scenario Outline: Find tag by id with an non existing id should return correct list
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When call find tag by id with id="<bad_id>"
    Then the returned http status for tag is 404
    Examples:
      |bad_id                                  |
      |72b9471b-aedf-4a19-948f-26a786d2a6a9|
      |21777046-b681-41e4-b2c0-26c1e9ed4533|

  Scenario Template: add tag should return 201
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    And tag name = "<name>"
    And  tag description = "<description>"
    When call add tag
    Then the returned http status for tag is 201
    And the created tag has properties name="<name>", description="<description>"
    Examples:
      |name    |description    |
      |tag 11 |description 11 |
      |tag 12 |description 12 |

  Scenario: add tag with an existing name should return 409
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When tag name = "tag 1"
    And  tag description = "description 1"
    When call add tag
    Then the returned http status for tag is 409

  Scenario Outline: update an existing tag should return 202
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    And tag name = "<name>"
    And tag description = "<description>"
    When call update tag with id="<id>"
    Then the returned http status for tag is 202
    And the updated tag has properties name="<name>", description="<description>"
    Examples:
      |id                                  |name   |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1.1|description 1.1|

  Scenario: update an non existing todo should return 404
    Given tag name = "tag 1"
    And  tag description = "description 1"
    When call update tag with id="82b9471b-aedf-4a19-948f-26a786d2a6a9"
    Then the returned http status for tag is 404

  Scenario: delete an existing tag should return 204
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When call delete tag with id="82b9471b-aedf-4a19-948f-26a786d2a6a9"
    Then the returned http status for tag is 204

  Example: delete an non existing tag should return 404
    Given acicd_tags table contains data:
      |id                                  |name  |description  |
      |82b9471b-aedf-4a19-948f-26a786d2a6a9|tag 1|description 1|
      |11777046-b681-41e4-b2c0-26c1e9ed4533|tag 2|description 2|
    When call delete tag with id="92b9471b-aedf-4a19-948f-26a786d2a6a9"
    Then the returned http status for tag is 404