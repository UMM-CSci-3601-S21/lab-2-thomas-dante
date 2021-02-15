package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.javalin.core.validation.Validator;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import umm3601.Server;

/**
 * Tests the logic of the ToDoController
 *
 * @throws IOException
 */
public class ToDoControllerSpec {
  private Context ctx = mock(Context.class);

  private ToDoController todoController;
  private static ToDoDatabase db;

  @BeforeEach
  public void setUp() throws IOException {
    ctx.clearCookieStore();

    db = new ToDoDatabase(Server.TODO_DATA_FILE);
    todoController = new ToDoController(db);
  }

  @Test
  public void GET_to_request_all_todos() throws IOException {
    // Call the method on the mock controller
    todoController.getTodos(ctx);

    // Confirm that `json` was called with all the todos.
    ArgumentCaptor<ToDo[]> argument = ArgumentCaptor.forClass(ToDo[].class);
    verify(ctx).json(argument.capture());
    assertEquals(db.size(), argument.getValue().length);
  }

  @Test
  public void GET_to_request_todo_with_existent_id() throws IOException {
    when(ctx.pathParam("id", String.class)).thenReturn(new Validator<String>("5889598520637f1dc4913e85", "", "id"));
    todoController.getTodo(ctx);
    verify(ctx).status(201);
  }

  @Test
  public void GET_to_request_todo_with_nonexistent_id() throws IOException {
    when(ctx.pathParam("id", String.class)).thenReturn(new Validator<String>("nonexistent", "", "id"));
    Assertions.assertThrows(NotFoundResponse.class, () -> {
      todoController.getTodo(ctx);
    });
  }

  @Test
  public void GET_to_request_contains_Sunt_todos() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("contains", Arrays.asList(new String[] { "Sunt" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    todoController.getTodos(ctx);

    // Confirm that all the todos contain Sunt.
    ArgumentCaptor<ToDo[]> argument = ArgumentCaptor.forClass(ToDo[].class);
    verify(ctx).json(argument.capture());
    for (ToDo todo : argument.getValue()) {
      assertTrue(todo.body.contains("Sunt"));
    }
  }

  @Test
  public void GET_to_request_owner_Fry_todos() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("owner", Arrays.asList(new String[] { "Fry" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    todoController.getTodos(ctx);

    // Confirm that all the todos are owned by Fry.
    ArgumentCaptor<ToDo[]> argument = ArgumentCaptor.forClass(ToDo[].class);
    verify(ctx).json(argument.capture());
    for (ToDo todo : argument.getValue()) {
      assertEquals("Fry", todo.owner);
    }
  }

  @Test
  public void GET_to_request_order_by_owner() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] { "owner" }));

    ToDo[] order = db.listTodos(queryParams);
    for(int i = 0;i < order.length-1;i++){
      assertTrue(order[i].owner.compareTo(order[i+1].owner) <= 0);
    }
    }

  @Test
  public void GET_to_request_order_by_body() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] { "body" }));

    ToDo[] order = db.listTodos(queryParams);
    for(int i = 0;i < order.length-1;i++){
      assertTrue(order[i].body.compareTo(order[i+1].body) <= 0);
    }
    }

  @Test
  public void GET_to_request_order_by_status() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] { "status" }));

    ToDo[] order = db.listTodos(queryParams);
    for(int i = 0;i < order.length-1;i++){
      assertTrue(Boolean.toString(order[i].status).compareTo(Boolean.toString(order[i+1].status)) <= 0);
    }
    }

  @Test
  public void GET_to_request_order_by_category() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] { "category" }));

    ToDo[] order = db.listTodos(queryParams);
    for(int i = 0;i < order.length-1;i++){
      assertTrue(order[i].category.compareTo(order[i+1].category) <= 0);
    }
    }


  @Test
  public void GET_to_request_category_homework_todos() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("category", Arrays.asList(new String[] { "homework" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    todoController.getTodos(ctx);

    // Confirm that all the todos are in the 'homework' category.
    ArgumentCaptor<ToDo[]> argument = ArgumentCaptor.forClass(ToDo[].class);
    verify(ctx).json(argument.capture());
    for (ToDo todo : argument.getValue()) {
      assertEquals("homework", todo.category);
    }
  }

  @Test
  public void GET_to_request_todos_with_limit() {
    // We'll set the requested "limit" to be 15.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] { "15" }));

    ToDo[] limitTwoTodos = db.listTodos(queryParams);
    assertEquals(15, limitTwoTodos.length, "Incorrect number of todos listed, not 2");
  }

  @Test
  public void GET_to_request_todos_with_illegal_limit() {
    // We'll set the requested "limit" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] { "abc" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    // This should now throw a `BadRequestResponse` exception because
    // our request has an limit that can't be parsed to a number.
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      todoController.getTodos(ctx);
    });
  }

  @Test
  public void GET_to_request_status_true() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] { "complete" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    todoController.getTodos(ctx);

    // Confirm that all the todos have status true.
    ArgumentCaptor<ToDo[]> argument = ArgumentCaptor.forClass(ToDo[].class);
    verify(ctx).json(argument.capture());
    for (ToDo todo : argument.getValue()) {
      assertTrue(todo.status);
    }
  }

  @Test
  public void GET_to_request_status_false() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("status", Arrays.asList(new String[] { "incomplete" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    todoController.getTodos(ctx);

    // Confirm that all the todos have status false.
    ArgumentCaptor<ToDo[]> argument = ArgumentCaptor.forClass(ToDo[].class);
    verify(ctx).json(argument.capture());
    for (ToDo todo : argument.getValue()) {
      assertFalse(todo.status);
    }
  }

}
