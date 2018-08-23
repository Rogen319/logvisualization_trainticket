package other.domain;

public class SuspendArea {

    private String suspendFromArea;

    private String suspendToArea;

    public SuspendArea() {
        //do nothing
    }

    public SuspendArea(String suspendFromArea, String suspendToArea) {
        this.suspendFromArea = suspendFromArea;
        this.suspendToArea = suspendToArea;
    }

    public String getSuspendFromArea() {
        return suspendFromArea;
    }

    public void setSuspendFromArea(String suspendFromArea) {
        this.suspendFromArea = suspendFromArea;
    }

    public String getSuspendToArea() {
        return suspendToArea;
    }

    public void setSuspendToArea(String suspendToArea) {
        this.suspendToArea = suspendToArea;
    }
}
