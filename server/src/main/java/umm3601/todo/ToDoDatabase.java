package umm3601.todo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import io.javalin.http.BadRequestResponse;

/**
 * A fake "database" of ToDo info
 * <p>
 * Since we don't want to complicate this lab with a real database, we're going
 * to instead just read a bunch of ToDo data from a specified JSON file, and
 * then provide various database-like methods that allow the `ToDoController` to
 * "query" the "database".
 */
public class ToDoDatabase {

  private ToDo[] allTodos;

  public ToDoDatabase(String todoDataFile) throws IOException {
    Gson gson = new Gson();
    InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(todoDataFile));
    allTodos = gson.fromJson(reader, ToDo[].class);
  }

  public int size() {
    return allTodos.length;
  }

  /**
   * Get the single ToDo specified by the given ID. Return `null` if there is no
   * ToDo with that ID.
   *
   * @param id the ID of the desired ToDo
   * @return the ToDo with the given ID, or null if there is no ToDo with that ID
   */
  public ToDo getToDo(String id) {
    return Arrays.stream(allTodos).filter(x -> x._id.equals(id)).findFirst().orElse(null);
  }

  /**
   * Get an array of all the Todos satisfying the queries in the params.
   *
   * @param queryParams map of key-value pairs for the query
   * @return an array of all the Todos matching the given criteria
   */
  public ToDo[] listTodos(Map<String, List<String>> queryParams) {
    ToDo[] filteredTodos = allTodos;

    return filteredTodos;
  }


}
