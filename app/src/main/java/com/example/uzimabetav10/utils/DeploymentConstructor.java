package com.example.uzimabetav10.utils;

public class DeploymentConstructor {

    public String deployed_driver;
    public String deployed_ambulance;
    public String deployed_company;
    public String driver_number;



    public DeploymentConstructor(){


    }


    public DeploymentConstructor(String deployed_driver, String deployed_ambulance, String deployed_company,String driver_number) {
        this.deployed_driver = deployed_driver;
        this.deployed_ambulance = deployed_ambulance;
        this.deployed_company = deployed_company;
        this.driver_number = driver_number;
    }

    public String getDeployed_driver() {
        return deployed_driver;
    }

    public void setDeployed_driver(String deployed_driver) {
        this.deployed_driver = deployed_driver;
    }

    public String getDeployed_ambulance() {
        return deployed_ambulance;
    }

    public void setDeployed_ambulance(String deployed_ambulance) {
        this.deployed_ambulance = deployed_ambulance;
    }

    public String getDeployed_company() {
        return deployed_company;
    }

    public void setDeployed_company(String deployed_company) {
        this.deployed_company = deployed_company;
    }
    public String getDriver_number() {
        return driver_number;
    }

    public void setDriver_number(String driver_number) {
        this.driver_number = driver_number;
    }

}
