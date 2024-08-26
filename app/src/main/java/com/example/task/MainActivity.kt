package com.example.task

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.task.ui.theme.TaskTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskTheme {
                MainScreen()
            }
        }
    }
}

data class Task(
    val text: String,
    var isDone: Boolean = false
)


@Composable
fun MainScreen() {

    val listOfTasks = remember { mutableStateListOf(
        Task("Task 1", false),
        Task("Task 2", false),
        Task("Task 3", false)
    ) }
    val showDialog = remember { mutableStateOf(false) }
    val taskToAdd = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp, top = 21.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tasks", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showDialog.value = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Blue
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(listOfTasks.size) { index ->
                TaskItem(
                    task = listOfTasks[index],
                    onDelete = { listOfTasks.removeAt(index) },
                    onToggleDone = { listOfTasks[index].isDone = !listOfTasks[index].isDone }
                )
            }
        }

        if (showDialog.value) {
            AddTaskDialog(
                taskText = taskToAdd.value,
                onTextChange = { taskToAdd.value = it },
                onAdd = {
                    if (taskToAdd.value.isNotEmpty()) {
                        listOfTasks.add(Task(taskToAdd.value, false))
                        taskToAdd.value = ""
                    }
                    showDialog.value = false
                },
                onCancel = {
                    taskToAdd.value = ""
                    showDialog.value = false
                }
            )
        }
    }
}


@Composable
fun TaskItem(
    task: Task,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit
) {
    var isClicked by remember { mutableStateOf(false) }
    val offsetX by animateDpAsState(targetValue = if (isClicked) (-100).dp else 0.dp)

    // Define a transition that will animate the border color
    val transition = updateTransition(targetState = isClicked, label = "")
    val borderColor by transition.animateColor(
        label = "",
        transitionSpec = { tween(durationMillis = 1000) } // 1 second animation
    ) { clicked ->
        if (clicked) Color.Red else Color.Black
    }

    val scale = remember { Animatable(initialValue = 0.7f) }
    var repeatCount by remember { mutableStateOf(0) }

    if (isClicked) {
        LaunchedEffect(repeatCount) {
            if (repeatCount < 1000) {
                scale.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = {
                            OvershootInterpolator(2f).getInterpolation(it)
                        }
                    )
                )
                delay(400L)
                scale.animateTo(
                    targetValue = 0.6f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = {
                            OvershootInterpolator(2f).getInterpolation(it)
                        }
                    )
                )
                repeatCount++
            }
        }
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .offset(x = offsetX)  // Animate the task item based on the state
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor) // Use the animated border color
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .background(Color.White)
            .clickable {
                isClicked = !isClicked
            }
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = if (isClicked) Color.Red else Color.Black,
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale.value)
                    .clickable {
                        if (isClicked) {
                            isClicked = false // Trigger the border color animation
                            onDelete()
                        } else {
                            Toast.makeText(context, "Toggle the task to delete it", Toast.LENGTH_SHORT).show()
                        }
                    }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = {
                    onToggleDone()
                    isClicked = !isClicked // Toggle once to update state
                    isClicked = !isClicked // Toggle once to update state
                },
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = task.text,
                fontSize = 18.sp,
                color = if (task.isDone) Color.Gray else Color.Black,
                modifier = Modifier
                    .padding(10.dp)
            )
        }
    }
}




@Composable
fun AddTaskDialog(
    taskText: String,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text(text = "Add New Task") },
        text = {
            OutlinedTextField(
                value = taskText,
                onValueChange = onTextChange,
                label = { Text(text = "Task Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onAdd() }) {
                Text(text = "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TaskTheme {
        MainScreen()
    }
}
