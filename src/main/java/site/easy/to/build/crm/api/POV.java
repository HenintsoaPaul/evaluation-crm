package site.easy.to.build.crm.api;

public class POV {
    // public
    public static class Public {
    }

    public static class Expense extends Public {
    }

    public static class Dashboard extends Public {
    }

    // private
    public static class Private extends Public {
    }

    public static class Admin extends Private {
    }

}
