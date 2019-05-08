package hypervolume;

/**
 * IllegalNumberOfObjectives hold details when the number of objectives of an
 * argument don't match the list set up.
 * 
 * @author Jonathan Fieldsend
 * @version 29/04/2019
 */
public class IllegalNumberOfObjectivesException extends Exception
{
    public IllegalNumberOfObjectivesException() {
        super();
    }
    
    public IllegalNumberOfObjectivesException(String message) {
        super(message);
    }
}
