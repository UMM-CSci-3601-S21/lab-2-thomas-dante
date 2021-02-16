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

    // Filter owner if defined
    if (queryParams.containsKey("owner")) {
      String targetString = queryParams.get("owner").get(0);
      filteredTodos = filterTodosByOwner(filteredTodos, targetString);
    }

    // Filter category if defined
    if (queryParams.containsKey("category")) {
      String targetString = queryParams.get("category").get(0);
      filteredTodos = filterTodosByCategory(filteredTodos, targetString);
    }

    // Filter body if defined
    if (queryParams.containsKey("contains")) {
      String targetString = queryParams.get("contains").get(0);
      filteredTodos = filterTodosByBody(filteredTodos, targetString);
    }

    // Filter status if defined
    if (queryParams.containsKey("status")) {
      String targetString = queryParams.get("status").get(0);
      filteredTodos = filterTodosByStatus(filteredTodos, targetString);
    }

    // Order by desired order
    if (queryParams.containsKey("orderBy")) {
      String targetString = queryParams.get("orderBy").get(0);
      filteredTodos = sortTodosBy(filteredTodos, targetString);
    }

    //Limit number of todos shown if defined
    if (queryParams.containsKey("limit")) {
      String todoParam = queryParams.get("limit").get(0);
      try {
        int targetAge = Integer.parseInt(todoParam);
        filteredTodos = limitTodos(filteredTodos, targetAge);
      } catch (NumberFormatException e) {
        throw new BadRequestResponse("Specified limit '" + todoParam + "' can't be parsed to an integer");
      }
    }


    return filteredTodos;
  }

  /**
   * Get an array of all the todos ordered a specific way.
   *
   * @param todos        the list of todos to order
   * @param targetString the target way you want it ordered by
   * @return an array of all the todos in order
   */
  public ToDo[] sortTodosBy(ToDo[] todos, String targetString) {

    if("owner".equals(targetString)){
      Arrays.sort(todos, (first, second) -> {
        return first.owner.compareTo(second.owner);
      });
    }
    if("category".equals(targetString)){
      Arrays.sort(todos, (first, second) -> {
        return first.category.compareTo(second.category);
      });
    }
    if("body".equals(targetString)){
      Arrays.sort(todos, (first, second) -> {
        return first.body.compareTo(second.body);
      });
    }
    if("status".equals(targetString)){
      Arrays.sort(todos, (first, second) -> {
        return Boolean.toString(first.status).compareTo(Boolean.toString(second.status));
      });
    }
    return todos;
}

  /**
   * Get an array of all the todos with a specific owner.
   *
   * @param todos        the list of todos to filter by owner
   * @param targetOwner the target owner to look for
   * @return an array of all the todos from the given list that have the target
   *         string
   */
  public ToDo[] filterTodosByOwner(ToDo[] todos, String targetOwner) {
    return Arrays.stream(todos).filter(x -> x.owner.equals(targetOwner)).toArray(ToDo[]::new);
  }

  /**
   * Get an array of all the todos with a specific category.
   *
   * @param todos        the list of todos to filter by category
   * @param targetCategory the target category to look for
   * @return an array of all the todos from the given list that have the target
   *         string
   */
  public ToDo[] filterTodosByCategory(ToDo[] todos, String targetCategory) {
    return Arrays.stream(todos).filter(x -> x.category.equals(targetCategory)).toArray(ToDo[]::new);
  }

  /**
   * Get an array of all the todos having the target string in their body.
   *
   * @param todos        the list of todos to filter by strings
   * @param targetString the target string to look for
   * @return an array of all the todos from the given list that have the target
   *         string
   */
  public ToDo[] filterTodosByBody(ToDo[] todos, String targetString) {
    return Arrays.stream(todos).filter(x -> x.body.contains(targetString)).toArray(ToDo[]::new);
  }

  /**
   * Get an array of all the users having the target status.
   *
   * @param todos        the list of todos to filter by status
   * @param targetString the target status to look for
   * @return an array of all the todos from the given list that have the target
   *         status
   */
  public ToDo[] filterTodosByStatus(ToDo[] todos, String targetString) {
    ToDo[] status = null;
    if ("complete".equals(targetString)){
      status = Arrays.stream(todos).filter(x -> (Boolean.toString(x.status)).contains("true")).toArray(ToDo[]::new);
    }
    else{
      status = Arrays.stream(todos).filter(x -> Boolean.toString(x.status).contains("false")).toArray(ToDo[]::new);
    }
    return status;
  }

  /**
   * Only displays a limited amount of entires.
   *
   * @param todos     the list of users to limit the number of
   * @param targetLimit the maximum allowed entries
   * @return an array of max size the targetLimit
   */
  public ToDo[] limitTodos(ToDo[] todos, Integer targetLimit) {
    return Arrays.stream(todos).limit(targetLimit).toArray(ToDo[]::new);
  }

}
