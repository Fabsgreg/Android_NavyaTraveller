package navya.tech.navyatraveller;

/**
 * Created by gregoire.frezet on 21/04/2016.
 */

public class SavingAccount {

    private String password;
    private String email;
    private String lastName;
    private String firstName;
    private Double duration;
    private Double distance;
    private Boolean isConnected;
    private Integer tripAborted;
    private String phoneNumber;
    private Integer nbrTravel;

    public SavingAccount() {
        this.password = "";
        this.email = "";
        this.lastName = "";
        this.firstName = "";
        this.duration = 0.0;
        this.distance = 0.0;
        this.isConnected = false;
        this.tripAborted = 0;
        this.phoneNumber = "";
        this.nbrTravel = 0;
    }


    // Getter & Setter
    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setConnected(Boolean connected) {
        this.isConnected = connected;
    }

    public void setTripAborted(Integer tripAborted) {
        this.tripAborted = tripAborted;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setNbrTravel(Integer nbrTravel) {
        this.nbrTravel = nbrTravel;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public Double getDuration() {
        return duration;
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getTripAborted() {
        return tripAborted;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Integer getNbrTravel() {
        return nbrTravel;
    }
}
