#include <stdio.h>
#include <stdlib.h>
#include <Windows.h>
#include <time.h>
#include <conio.h>

void gotoxy(int x, int y)
{
	COORD coord;
	coord.X = x;
	coord.Y = y;
	SetConsoleCursorPosition(GetStdHandle(STD_OUTPUT_HANDLE), coord);
}
// 定义游戏区域的边界宽度
#define   WIDTH        100
#define   HIGH         20
// 定义蛇的最大长度
#define   MAX_LENGHT   100
// 定义游戏初始速度
#define   SPEED        500

// 定义游戏中的两个对象：食物和蛇
struct
{
	int x;
	int y;
}food;

struct
{
	int len;
	int x_buf[MAX_LENGHT];
	int y_buf[MAX_LENGHT];
	int score;
}snake;

int food_flag = 0;
int key = 72;

/* 
 *  绘制游戏区域边界
 */
void DrawMap(void)
{
	int x, y;
	for (x = 0; x < WIDTH + 4; x += 2)
	{
		y = 0;
		gotoxy(x, y);
		printf("■");
		y = HIGH + 2;
		gotoxy(x, y);
		printf("■");
	}

	for (y = 1; y < HIGH + 2; y++)
	{
		x = 0;
		gotoxy(x, y);
		printf("■");
		x = WIDTH + 2;
		gotoxy(x, y);
		printf("■");
	}

	// 将光标移出游戏区域
	gotoxy(0, HIGH + 5);
}
/*
 *  初始化小蛇
 */
void CreateSnake(void)
{
	int orgin_x, orgin_y;
	orgin_x = WIDTH / 2 + 2;
	orgin_y = HIGH / 2 + 1;

	snake.len = 3;
	snake.x_buf[0] = orgin_x;
	snake.y_buf[0] = orgin_y;
	snake.x_buf[1] = orgin_x;
	snake.y_buf[1] = ++orgin_y;
	snake.x_buf[2] = orgin_x;
	snake.y_buf[2] = ++orgin_y;

	snake.score = -1;

	int i;
	for (i = 0; i < snake.len; i++)
	{
		gotoxy(snake.x_buf[i], snake.y_buf[i]);
		printf("■");
	}
	gotoxy(0, HIGH + 5);
}
/*
 *  当游戏区域内不存在食物时，随机创造一个食物
 */
void CreateFood(void)
{
	if (food_flag == 0)
	{
		int flag = 0,i;
		do
		{
			srand((unsigned int)time(NULL));
			food.x = (rand() % (WIDTH/2))*2 + 2;
			food.y = rand() % HIGH + 1;

			// 判断生成的食物是否和蛇身重合
			for (i = 0; i < snake.len; i++)
			{
				if (snake.x_buf[i] == food.x && snake.y_buf[i] == food.y)
				{
					flag = 1;
					break;
				}
			}

		} while (flag);

		gotoxy(food.x, food.y);
		printf("★");

		// 吃到食物，则分数加1
		snake.score++;

		food_flag = 1;
	}
	gotoxy(0, HIGH + 5);
}
void SnakeMove(int x, int y)
{
	// 判断是否吃到食物，吃到长度加1
	if (!food_flag)
		snake.len++;
	// 没吃到则抹去最后一节
	else
	{
		gotoxy(snake.x_buf[snake.len - 1], snake.y_buf[snake.len - 1]);
		printf("  ");
	}
	int i;
	for (i = snake.len - 1; i > 0; i--)
	{
		snake.x_buf[i] = snake.x_buf[i - 1];
		snake.y_buf[i] = snake.y_buf[i - 1];
	}
	snake.x_buf[0] = x;
	snake.y_buf[0] = y;
	gotoxy(snake.x_buf[0], snake.y_buf[0]);
	printf("■");
	gotoxy(0, HIGH + 5);
}
void move()
{
	int pre_key = key, x, y;

	if (_kbhit())
	{
		fflush(stdin);
		key = _getch();
		key = _getch();
	}

	// 小蛇移动方向不能和上一次的方向相反
	if (pre_key == 72 && key == 80)
		key = 72;
	if (pre_key == 80 && key == 72)
		key = 80;
	if (pre_key == 75 && key == 77)
		key = 75;
	if (pre_key == 77 && key == 75)
		key = 77;

	switch (key)
	{
	case 75:
		x = snake.x_buf[0] - 2;//往左
		y = snake.y_buf[0];
		break;
	case 77:
		x = snake.x_buf[0] + 2;//往右
		y = snake.y_buf[0];
		break;
	case 72:
		x = snake.x_buf[0];
		y = snake.y_buf[0] - 1;//往上
		break;
	case 80:
		x = snake.x_buf[0];
		y = snake.y_buf[0] + 1;//往下
		break;
	}

	if (x == food.x&&y == food.y)
		food_flag = 0;
	SnakeMove(x, y);
}
void check(void)
{
	int i;

	// 失败条件
	if (snake.x_buf[0] == 0 | snake.x_buf[0] == WIDTH + 4 | snake.y_buf[0] == 0 | snake.y_buf[0] == HIGH + 2)
	{
		printf("Game Over!\n");
		exit(0);
	}
	for (i = 1; i < snake.len; i++)
	{
		if (snake.x_buf[0] == snake.x_buf[i] && snake.y_buf[0] == snake.y_buf[i])
		{
			printf("Game Over!\n");
			exit(0);
		}
	}

	// 胜利条件
	if (snake.len == MAX_LENGHT)
	{
		printf("Your are win!\n");
		exit(0);
	}

	// 打印得分
	gotoxy(0, HIGH + 6);
	printf("Your score: %d", snake.score);
}
int main(void)
{
	DrawMap();
	CreateSnake();
	while (1)
	{
		CreateFood();
		move();
		Sleep(SPEED - 2 * snake.len);
		check();
	}
}
