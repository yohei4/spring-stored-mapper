package io.storedmapper;

import io.storedmapper.annotation.DbParameterOrder;
import io.storedmapper.annotation.DbProgramName;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DbProgramBaseTest {

    // --- テスト用クラス ---

    static class SourceObject {
        private UUID userId;
        private String name;
        private Integer age;

        SourceObject(UUID userId, String name, Integer age) {
            this.userId = userId;
            this.name = name;
            this.age = age;
        }
    }

    @DbProgramName("sp_test")
    static class TargetParam extends DbProgramBase {
        @DbParameterOrder(1) private UUID userId;
        @DbParameterOrder(2) private String name;
        @DbParameterOrder(3) private Integer age;

        TargetParam(Object source) {
            super(source);
        }

        UUID getUserId() { return userId; }
        String getName() { return name; }
        Integer getAge() { return age; }
    }

    @DbProgramName("sp_test2")
    static class EmptyStringParam extends DbProgramBase {
        @DbParameterOrder(1) private String name;

        EmptyStringParam(Object source) {
            super(source);
        }

        String getName() { return name; }
    }

    static class EmptyStringSource {
        private String name = "";
    }

    @DbProgramName("sp_test3")
    static class ManualParam extends DbProgramBase {
        @DbParameterOrder(1) private String name;

        ManualParam() {}

        String getName() { return name; }
    }

    // --- テスト ---

    @Test
    void constructor_shouldCopyFieldsFromSource() {
        var userId = UUID.randomUUID();
        var source = new SourceObject(userId, "田中", 30);
        var param = new TargetParam(source);

        assertEquals(userId, param.getUserId());
        assertEquals("田中", param.getName());
        assertEquals(30, param.getAge());
    }

    @Test
    void constructor_shouldConvertEmptyStringToNull() {
        var source = new EmptyStringSource();
        var param = new EmptyStringParam(source);

        assertNull(param.getName());
    }

    @Test
    void constructor_shouldHandleNullSource() {
        var param = new TargetParam(null);

        assertNull(param.getUserId());
        assertNull(param.getName());
        assertNull(param.getAge());
    }

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        var param = new ManualParam();
        assertNull(param.getName());
    }
}
