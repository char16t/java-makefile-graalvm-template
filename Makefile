SOURCE_DIR            = src/main/java
RESOURCE_DIR          = src/main/resources
TEST_SOURCE_DIR       = src/test/java
TEST_RESOURCE_DIR     = src/test/resources
TARGET_CLASS_DIR      = target/classes
TARGET_TEST_CLASS_DIR = target/test-classes

JAVA_COMPILER       = javac
JAVA_COMPILER_FLAGS = -d $(TARGET_CLASS_DIR)/ -cp $(SOURCE_DIR)/

RWILDCARD    = $(foreach d,$(wildcard $(1:=/*)),$(call RWILDCARD,$d,$2) $(filter $(subst *,%,$2),$d))
SOURCES      = $(call RWILDCARD,$(SOURCE_DIR),*.java)
CLASSES      = $(SOURCES:$(SOURCE_DIR)/%.java=$(TARGET_CLASS_DIR)/%.class)
TEST_SOURCES = $(call RWILDCARD,$(TEST_SOURCE_DIR),*.java)
TEST_CLASSES = $(SOURCES:$(TEST_SOURCE_DIR)/%.java=$(TARGET_TEST_CLASS_DIR)/%.class)

.PHONY: all clean

all: $(CLASSES)

$(CLASSES): $(TARGET_CLASS_DIR)/%.class: $(SOURCE_DIR)/%.java
	$(JAVA_COMPILER) $(JAVA_COMPILER_FLAGS) $<

clean:
	rm -rf target/


