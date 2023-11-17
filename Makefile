SOURCE_DIR            = src/main/java
RESOURCE_DIR          = src/main/resources
TEST_SOURCE_DIR       = src/test/java
TEST_RESOURCE_DIR     = src/test/resources
TARGET_DIR            = target
TARGET_CLASS_DIR      = $(TARGET_DIR)/classes
TARGET_TEST_CLASS_DIR = $(TARGET_DIR)/test-classes
TARGET_JAVADOC_DIR    = $(TARGET_DIR)/javadoc

JAVA_COMPILER   = javac
JAVA_PACKAGE    = com.manenkov
MAIN_CLASS      = $(JAVA_PACKAGE).Application
MAIN_TEST_CLASS = $(JAVA_PACKAGE).ApplicationTest

RWILDCARD    = $(foreach d,$(wildcard $(1:=/*)),$(call RWILDCARD,$d,$2) $(filter $(subst *,%,$2),$d))
SOURCES      = $(call RWILDCARD,$(SOURCE_DIR),*.java)
CLASSES      = $(SOURCES:$(SOURCE_DIR)/%.java=$(TARGET_CLASS_DIR)/%.class)
TEST_SOURCES = $(call RWILDCARD,$(TEST_SOURCE_DIR),*.java)
TEST_CLASSES = $(TEST_SOURCES:$(TEST_SOURCE_DIR)/%.java=$(TARGET_TEST_CLASS_DIR)/%.class)

.PHONY: all clean test javadoc package-sources jar native-image

all: test jar native-image

$(CLASSES): $(TARGET_CLASS_DIR)/%.class: $(SOURCE_DIR)/%.java
	$(JAVA_COMPILER) -d $(TARGET_CLASS_DIR)/ -cp $(SOURCE_DIR)/ $<

$(TEST_CLASSES): $(TARGET_TEST_CLASS_DIR)/%.class: $(TEST_SOURCE_DIR)/%.java
	$(JAVA_COMPILER) -d $(TARGET_TEST_CLASS_DIR)/ -cp "$(TEST_SOURCE_DIR)/:$(SOURCE_DIR)" $<

test: $(TEST_CLASSES)
	cp -r $(RESOURCE_DIR)/* $(TARGET_TEST_CLASS_DIR)
	cp -rf $(TEST_RESOURCE_DIR)/* $(TARGET_TEST_CLASS_DIR)
	@echo "Manifest-Version: 1.0" > $(TARGET_DIR)/manifest-test.txt
	@echo "Class-Path: ." >> $(TARGET_DIR)/manifest-test.txt
	@echo "Main-Class: $(MAIN_TEST_CLASS)" >> $(TARGET_DIR)/manifest-test.txt
	@echo "" >> $(TARGET_DIR)/manifest-test.txt
	jar -cmf $(TARGET_DIR)/manifest-test.txt $(TARGET_DIR)/application-test.jar -C $(TARGET_TEST_CLASS_DIR) .
	java -jar $(TARGET_DIR)/application-test.jar

package-sources:
	mkdir -p $(TARGET_DIR)
	jar --create --file=$(TARGET_DIR)/application-sources.jar -C $(SOURCE_DIR) .
	jar --update --file=$(TARGET_DIR)/application-sources.jar -C $(RESOURCE_DIR) .

javadoc:
	javadoc -d $(TARGET_JAVADOC_DIR) -sourcepath $(SOURCE_DIR) $(JAVA_PACKAGE)
	jar --create --file=$(TARGET_DIR)/application-javadoc.jar -C $(TARGET_JAVADOC_DIR) .

jar: $(CLASSES)
	cp -r $(RESOURCE_DIR)/* $(TARGET_CLASS_DIR)
	@echo "Manifest-Version: 1.0" > $(TARGET_DIR)/manifest.txt
	@echo "Class-Path: ." >> $(TARGET_DIR)/manifest.txt
	@echo "Main-Class: $(MAIN_CLASS)" >> $(TARGET_DIR)/manifest.txt
	@echo "" >> $(TARGET_DIR)/manifest.txt
	jar -cmf $(TARGET_DIR)/manifest.txt $(TARGET_DIR)/application.jar -C $(TARGET_CLASS_DIR) .

native-image: jar
	native-image -H:ResourceConfigurationFiles=$(TARGET_CLASS_DIR)/resource-config.json \
		-jar $(TARGET_DIR)/application.jar $(TARGET_DIR)/application

clean:
	rm -rf target/
