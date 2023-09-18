import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.Socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;

public class ServerThread extends Thread
{
    private final Socket clientSocket;
    private final Server server;
    private String username;

    /**
     * Constructor for the ServerThread class
     * @param clientSocket The client socket that the thread will be using
     * @param server We use this to pass a reference to the server object, so we can use it for the log method
     */
    public ServerThread(Socket clientSocket, Server server)
    {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    /**
     * This method is called when the thread starts and runs the code for the login
     */
    @Override
    public void run()
    {
        try
        {
            // Print a message when the thread starts to do some logging for the thread
//            System.out.println("Thread for client started: " + Thread.currentThread().getId());

            // Initialize BufferedReader that will read the login data (username, password) from the client socket
            BufferedReader loginData = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Initialize BufferedWriter that will send the result of the login check to the client socket
            BufferedWriter response = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String username = loginData.readLine();
            String password = loginData.readLine();

//            System.out.println(username + " " + password);

            // Check if the username and password are correct through the SQL query.
            boolean checkCredentials = checkCredentialsQuery(username,password);
            String ok = String.valueOf(checkCredentials);

            if(checkCredentials)
            {
                this.username = username;
                server.logMessage("User " + this.username + " logged in successfully");
                // System.out.println("Client with username " + this.username + " logged in successfully");
            }else
            {
                server.logMessage("User " + username + " failed to log in");
                // System.out.println("Client with username " + username + " failed to log in");
            }

            System.out.println(ok);

            response.write(ok);
            response.newLine();
            response.flush();

            // Print a message when the thread finishes to do some logging for the thread
//            System.out.println("Thread for client finished: " + Thread.currentThread().getId());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(clientSocket != null && !clientSocket.isClosed())
                {
                    clientSocket.close();
                    server.logMessage("User " + this.username + " disconnected");
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method checks if the username and password are correct through an SQL query
     * @param username The username that the client sent and that we use in the query
     * @param password The password that the client sent and that we use in the query
     * @return Returns true if the username and password are correct, false otherwise
     */
    public boolean checkCredentialsQuery(String username , String password)
    {
        try
        {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/optikitec", "root", "root");

            // using a string for the SQL query
            String checkQuery = "SELECT * FROM users WHERE username = ? AND password = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next())
            {
                // JOptionPane.showMessageDialog(this, "This user already exists");
                //System.out.println("User with username " + username + " already exists");
                return true;
            }
            else
            {
                System.out.println("User with username " + username + " does not exist");
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
            return false;
        }
        catch (SQLIntegrityConstraintViolationException key)
        {
            System.out.println("Username already exists. Registration failed!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
