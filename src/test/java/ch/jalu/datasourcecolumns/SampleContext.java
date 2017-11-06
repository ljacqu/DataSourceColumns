package ch.jalu.datasourcecolumns;

public class SampleContext {

    private boolean isEmailEmpty;
    private boolean isIsLockedEmpty;
    private boolean isLastLoginEmpty;

    private boolean useDefaultForIsActive;
    private boolean useDefaultForLastLogin;

    public String resolveName(SampleColumns<?> col) {
        if (col == SampleColumns.NAME) {
            return "username";
        } else if (col == SampleColumns.ID) {
            return "id";
        } else if (col == SampleColumns.EMAIL) {
            return isEmailEmpty ? "" : "email";
        } else if (col == SampleColumns.IS_LOCKED) {
            return isIsLockedEmpty ? "" : "is_locked";
        } else if (col == SampleColumns.IS_ACTIVE) {
            return "is_active";
        } else if (col == SampleColumns.LAST_LOGIN) {
            return isLastLoginEmpty ? "" : "last_login";
        } else if (col == SampleColumns.IP) {
            return "ip";
        } else {
            throw new IllegalStateException("Unknown sample column '" + col + "'");
        }
    }

    public boolean resolveUseDefaultForNull(SampleColumns<?> col) {
        if (col == SampleColumns.IS_ACTIVE) {
            return useDefaultForIsActive;
        } else if (col == SampleColumns.LAST_LOGIN) {
            return useDefaultForLastLogin;
        } else {
            return false;
        }
    }

    public void setEmptyOptions(boolean isEmailEmpty, boolean isIsLockedEmpty, boolean isLastLoginEmpty) {
        this.isEmailEmpty = isEmailEmpty;
        this.isIsLockedEmpty = isIsLockedEmpty;
        this.isLastLoginEmpty = isLastLoginEmpty;
    }

    public void setUseDefaults(boolean useDefaultForIsActive, boolean useDefaultForLastLogin) {
        this.useDefaultForIsActive = useDefaultForIsActive;
        this.useDefaultForLastLogin = useDefaultForLastLogin;
    }

    @Override
    public String toString() {
        return "empty{email=" + isEmailEmpty
            + ", isLocked=" + isIsLockedEmpty
            + ", lastLogin=" + isLastLoginEmpty
            + "}";
    }
}
