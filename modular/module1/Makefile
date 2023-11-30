SOURCE_DIR            = src/main/java
RESOURCE_DIR          = src/main/resources
INTEGRATION_TEST_DIR  = src/it
TEST_SOURCE_DIR       = src/test/java
TEST_RESOURCE_DIR     = src/test/resources
TARGET_DIR            = target
TARGET_CLASS_DIR      = $(TARGET_DIR)/classes
TARGET_TEST_CLASS_DIR = $(TARGET_DIR)/test-classes
TARGET_IT_CLASS_DIR   = $(TARGET_DIR)/integration-test-classes
NI_PROFILE_DATA_DIR   = $(TARGET_DIR)/profile-data

JAVA_PACKAGE            = com.manenkov.module1
MAIN_CLASS              = $(JAVA_PACKAGE).Module1
JAR_NAME                = module1.jar
NATIVE_IMAGE_NAME       = module1
MAIN_TEST_CLASS         = $(JAVA_PACKAGE).Module1Test
TEST_JAR_NAME           = run-tests.jar
TEST_NATIVE_IMAGE_NAME  = run-tests
PROFILE_JAR_NAME        = module1-native.jar
PROFILE_NI_NAME         = module1-profile
PROFILE_IPROF_LIST_FILE = all.txt

JAVA_COMPILER = javac -source 21 --enable-preview
NATIVE_IMAGE  = native-image -H:+UnlockExperimentalVMOptions \
	--no-fallback --strict-image-heap -R:MaxHeapSize=1G -march=compatibility --gc=G1 \
	--enable-preview

RWILDCARD         = $(foreach d,$(wildcard $(1:=/*)),$(call RWILDCARD,$d,$2) $(filter $(subst *,%,$2),$d))
SOURCES           = $(call RWILDCARD,$(SOURCE_DIR),*.java)
CLASSES           = $(SOURCES:$(SOURCE_DIR)/%.java=$(TARGET_CLASS_DIR)/%.class)
TEST_SOURCES      = $(call RWILDCARD,$(TEST_SOURCE_DIR),*.java)
TEST_CLASSES      = $(TEST_SOURCES:$(TEST_SOURCE_DIR)/%.java=$(TARGET_TEST_CLASS_DIR)/%.class)

.PHONY: all clean jar native-image test test/jar test/native build-integration-test

all: clean test native-image

$(CLASSES): $(TARGET_CLASS_DIR)/%.class: $(SOURCE_DIR)/%.java
	$(JAVA_COMPILER) -d $(TARGET_CLASS_DIR)/ -cp $(SOURCE_DIR) $<

$(TEST_CLASSES): $(TARGET_TEST_CLASS_DIR)/%.class: $(TEST_SOURCE_DIR)/%.java
	$(JAVA_COMPILER) -d $(TARGET_TEST_CLASS_DIR)/ -cp "$(TEST_SOURCE_DIR)/:$(SOURCE_DIR)" $<

test: test/jar test/native

test/jar: $(TEST_CLASSES)
	cp -r $(RESOURCE_DIR)/* $(TARGET_TEST_CLASS_DIR)
	cp -r $(TEST_RESOURCE_DIR)/* $(TARGET_TEST_CLASS_DIR)
	@echo "Manifest-Version: 1.0" > $(TARGET_DIR)/manifest-test.txt
	@echo "Class-Path: ." >> $(TARGET_DIR)/manifest-test.txt
	@echo "Main-Class: $(MAIN_TEST_CLASS)" >> $(TARGET_DIR)/manifest-test.txt
	@echo "" >> $(TARGET_DIR)/manifest-test.txt
	jar -cmf $(TARGET_DIR)/manifest-test.txt $(TARGET_DIR)/$(TEST_JAR_NAME) -C $(TARGET_TEST_CLASS_DIR) .
	java -ea -jar $(TARGET_DIR)/$(TEST_JAR_NAME)

test/native: test/jar
	$(NATIVE_IMAGE) -ea -jar $(TARGET_DIR)/$(TEST_JAR_NAME) $(TARGET_DIR)/$(TEST_NATIVE_IMAGE_NAME)
	$(TARGET_DIR)/$(TEST_NATIVE_IMAGE_NAME)

build-integration-test:
	$(JAVA_COMPILER) -d $(TARGET_IT_CLASS_DIR) $(INTEGRATION_TEST_DIR)/IntegrationTest.java

jar: $(CLASSES)
	# Copy resources
	cp -r $(RESOURCE_DIR)/* $(TARGET_CLASS_DIR)
	# Create Manifest
	@echo "Manifest-Version: 1.0" > $(TARGET_DIR)/manifest.txt
	@echo "Class-Path: ." >> $(TARGET_DIR)/manifest.txt
	@echo "Main-Class: $(MAIN_CLASS)" >> $(TARGET_DIR)/manifest.txt
	@echo "" >> $(TARGET_DIR)/manifest.txt
	# Package
	jar -cmf $(TARGET_DIR)/manifest.txt $(TARGET_DIR)/$(JAR_NAME) -C $(TARGET_CLASS_DIR) .

native-image: jar build-integration-test
	# Collect information for native-image (reflection, jni, etc.)
	java -cp $(TARGET_IT_CLASS_DIR) IntegrationTest $(NI_PROFILE_DATA_DIR) $(PROFILE_IPROF_LIST_FILE) \
		java -agentlib:native-image-agent=config-merge-dir=$(TARGET_CLASS_DIR)/META-INF/native-image,experimental-class-define-support -jar $(TARGET_DIR)/$(JAR_NAME)
	# Rebuild jar with collected information
	jar -cmf $(TARGET_DIR)/manifest.txt $(TARGET_DIR)/$(PROFILE_JAR_NAME) -C $(TARGET_CLASS_DIR) .
	# Build native-image for Profile-Guided Optimizations
	$(NATIVE_IMAGE) --pgo-instrument -jar $(TARGET_DIR)/$(PROFILE_JAR_NAME) $(TARGET_DIR)/$(PROFILE_NI_NAME)
	# Collect information for Profile-Guided Optimizations
	java -cp $(TARGET_IT_CLASS_DIR) IntegrationTest $(NI_PROFILE_DATA_DIR) $(PROFILE_IPROF_LIST_FILE) $(TARGET_DIR)/$(PROFILE_NI_NAME)
	# Build optimized native-image
	$(NATIVE_IMAGE) --static --pgo=$$(cat $(NI_PROFILE_DATA_DIR)/$(PROFILE_IPROF_LIST_FILE)), -jar $(TARGET_DIR)/$(JAR_NAME) $(TARGET_DIR)/$(NATIVE_IMAGE_NAME)

clean:
	rm -rf $(TARGET_DIR)
