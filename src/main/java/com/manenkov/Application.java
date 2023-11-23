package com.manenkov;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

public class Application {

    static final int EXIT_SUCCESS = 0;
    static final int EXIT_FAILURE = 1;

    class InputBuffer {
        char[] buffer;
        int bufferLength;
        int inputLength;
    }

    enum ExecuteResult {
        EXECUTE_SUCCESS,
        EXECUTE_DUPLICATE_KEY
    }

    enum MetaCommandResult {
        META_COMMAND_SUCCESS,
        META_COMMAND_UNRECOGNIZED_COMMAND
    }

    enum PrepareResult {
        PREPARE_SUCCESS,
        PREPARE_NEGATIVE_ID,
        PREPARE_STRING_TOO_LONG,
        PREPARE_SYNTAX_ERROR,
        PREPARE_UNRECOGNIZED_STATEMENT
    }

    enum StatementType {
        STATEMENT_INSERT,
        STATEMENT_SELECT
    }

    static final int COLUMN_USERNAME_SIZE = 32;
    static final int COLUMN_EMAIL_SIZE = 255;
    class Row {
        int id;
        char[] username = new char[COLUMN_USERNAME_SIZE + 1];
        char[] email = new char[COLUMN_EMAIL_SIZE + 1];
    }

    class Statement {
        StatementType type;
        Row rowToInsert; // only used by insert statement
    }

    static final int ID_SIZE = Integer.BYTES;
    static final int USERNAME_SIZE = Character.BYTES * (COLUMN_USERNAME_SIZE + 1);
    static final int EMAIL_SIZE = Character.BYTES * (COLUMN_EMAIL_SIZE + 1);
    static final int ID_OFFSET = 0;
    static final int USERNAME_OFFSET = ID_OFFSET + ID_SIZE;
    static final int EMAIL_OFFSET = USERNAME_OFFSET + USERNAME_SIZE;
    static final int ROW_SIZE = ID_SIZE + USERNAME_SIZE + EMAIL_SIZE;

    static final int PAGE_SIZE = 4096; 
    static final int TABLE_MAX_PAGES = 400;
    static final int INVALID_PAGE_NUM = Integer.MAX_VALUE;

    class Pager {
        RandomAccessFile fileDescriptor;
        long fileLength;
        long numPages;
        Object[] pages = new Object[TABLE_MAX_PAGES]; // void* pages[TABLE_MAX_PAGES];
    }

    class Table {
        Pager pager;
        int rootPageNum;
    }

    class Cursor {
        Table table;
        int pageNum;
        int cellNum;
        boolean endOfTable; // Indicates a position one past the last element
    }

    void printRow(Row row) {
        System.out.println(String.format("(%d, %s, %s)", row.id, row.username, row.email));
    }

    enum NodeType {
        NODE_INTERNAL,
        NODE_LEAF
    }

    // Common Node Header Layout
    static final int NODE_TYPE_SIZE = Byte.BYTES;
    static final int NODE_TYPE_OFFSET = 0;
    static final int IS_ROOT_SIZE = Byte.BYTES;
    static final int IS_ROOT_OFFSET = NODE_TYPE_SIZE;
    static final int PARENT_POINTER_SIZE = Integer.BYTES;
    static final int PARENT_POINTER_OFFSET = IS_ROOT_OFFSET + IS_ROOT_SIZE;
    static final int COMMON_NODE_HEADER_SIZE = NODE_TYPE_SIZE + IS_ROOT_SIZE + PARENT_POINTER_SIZE;

    // Internal Node Header Layout
    static final int INTERNAL_NODE_NUM_KEYS_SIZE = Integer.BYTES;
    static final int INTERNAL_NODE_NUM_KEYS_OFFSET = COMMON_NODE_HEADER_SIZE;
    static final int INTERNAL_NODE_RIGHT_CHILD_SIZE = Integer.BYTES;
    static final int INTERNAL_NODE_RIGHT_CHILD_OFFSET = INTERNAL_NODE_NUM_KEYS_OFFSET + INTERNAL_NODE_NUM_KEYS_SIZE;
    static final int INTERNAL_NODE_HEADER_SIZE = COMMON_NODE_HEADER_SIZE + INTERNAL_NODE_NUM_KEYS_SIZE + INTERNAL_NODE_RIGHT_CHILD_SIZE;

    // Internal Node Body Layout
    static final int INTERNAL_NODE_KEY_SIZE = Integer.BYTES;
    static final int INTERNAL_NODE_CHILD_SIZE = Integer.BYTES;
    static final int INTERNAL_NODE_CELL_SIZE = INTERNAL_NODE_CHILD_SIZE + INTERNAL_NODE_KEY_SIZE;
    static final int INTERNAL_NODE_MAX_KEYS = 3; // Keep this small for testing

    // Leaf Node Header Layout
    static final int LEAF_NODE_NUM_CELLS_SIZE = Integer.BYTES;
    static final int LEAF_NODE_NUM_CELLS_OFFSET = COMMON_NODE_HEADER_SIZE;
    static final int LEAF_NODE_NEXT_LEAF_SIZE = Integer.BYTES;
    static final int LEAF_NODE_NEXT_LEAF_OFFSET = LEAF_NODE_NUM_CELLS_OFFSET + LEAF_NODE_NUM_CELLS_SIZE;
    static final int LEAF_NODE_HEADER_SIZE = COMMON_NODE_HEADER_SIZE + LEAF_NODE_NUM_CELLS_SIZE + LEAF_NODE_NEXT_LEAF_SIZE;

    // Leaf Node Body Layout
    static final int LEAF_NODE_KEY_SIZE = Integer.BYTES;
    static final int LEAF_NODE_KEY_OFFSET = 0;
    static final int LEAF_NODE_VALUE_SIZE = ROW_SIZE;
    static final int LEAF_NODE_VALUE_OFFSET = LEAF_NODE_KEY_OFFSET + LEAF_NODE_KEY_SIZE;
    static final int LEAF_NODE_CELL_SIZE = LEAF_NODE_KEY_SIZE + LEAF_NODE_VALUE_SIZE;
    static final int LEAF_NODE_SPACE_FOR_CELLS = PAGE_SIZE - LEAF_NODE_HEADER_SIZE;
    static final int LEAF_NODE_MAX_CELLS = LEAF_NODE_SPACE_FOR_CELLS / LEAF_NODE_CELL_SIZE;
    static final int LEAF_NODE_RIGHT_SPLIT_COUNT = (LEAF_NODE_MAX_CELLS + 1) / 2;
    static final int LEAF_NODE_LEFT_SPLIT_COUNT = (LEAF_NODE_MAX_CELLS + 1) - LEAF_NODE_RIGHT_SPLIT_COUNT;

    // TODO: get_node_type
    // TODO: set_node_type
    // TODO: is_node_root
    // TODO: set_node_root
    // ...

    Pager pagerOpen(String filename) {
        final File file = new File(filename);
        try(final RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            final long fileLength = file.length();
            if (fileLength % PAGE_SIZE != 0) {
                System.out.println("Db file is not a whole number of pages. Corrupt file.");
                System.exit(EXIT_FAILURE);
            }
            final Pager pager = new Pager();
            pager.fileDescriptor = raf;
            pager.fileLength = fileLength;
            pager.numPages = fileLength / PAGE_SIZE;
            for (int i = 0; i < TABLE_MAX_PAGES; i++) {
                pager.pages[i] = null; 
            }
            return pager;
        } catch (final IOException exception) {
            System.out.println("Unable to open file.");
            System.exit(EXIT_FAILURE);
        }
        return null;
    }

    Object getPage(Pager pager, int pageNum) {
        if (pageNum > TABLE_MAX_PAGES) {
            System.out.println(String.format("Tried to fetch page number out of bounds. %d > %d", pageNum, TABLE_MAX_PAGES));
        }
        if (pager.pages[pageNum] == null) {
            // Cache miss. Allocate memory and load from file.
            byte[] page = new byte[PAGE_SIZE];
            long numPages = pager.fileLength / PAGE_SIZE;

            // We might save a partial page at the end of the file
            if (pager.fileLength % PAGE_SIZE != 0) {
                numPages++;
            }

            if (pageNum <= numPages) {
                try {
                    RandomAccessFile file = pager.fileDescriptor;
                    file.seek(pageNum * PAGE_SIZE);
                    int bytesRead = file.read(page);
                    if (bytesRead == -1) {
                        System.out.println("Error reading file: " + file);
                        System.exit(EXIT_FAILURE);
                    }
                } catch (IOException e) {
                    System.out.println("Error reading file: ");
                    e.printStackTrace();
                    System.exit(EXIT_FAILURE);
                }
            }

            pager.pages[pageNum] = page;

            if (pageNum >= pager.numPages) {
                pager.numPages = pageNum + 1;
            }
        }
        return pager.pages[pageNum];
    }

    void initializeLeafNode(Object rootNode) {
        // TODO
        // setNodeType(rootNode, NODE_LEAF);
        //setNodeRoot(rootNode, false);
        // TODO ...
    }

    Table dbOpen(String filename) {
        final Pager pager = pagerOpen(filename);
        final Table table = new Table();
        table.pager = pager;
        table.rootPageNum = 0;
        if (pager.numPages == 0) {
            // New database file. Initialize page 0 as leaf node.
            // TODO:
            Object rootNode = getPage(pager, 0);
            initializeLeafNode(rootNode);
            //setNodeRoot(rootNode, true);
        }
        return table;
    }

    // ...

    void execute() throws Exception {
        dbOpen("target/out3");
        System.out.println("It's works!");
    }

    public static void main(final String[] args) throws Exception {
        new Application().execute();
    }
}
