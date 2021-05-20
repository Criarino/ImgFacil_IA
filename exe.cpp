//Pequeno programa em C++ usado para "amarrar" as partes do código. Todos os processos são chamados por ele
#include <fstream>
#include <stdio.h>
#include <string>
#include <vector>

using namespace std;

int main()
{
	ifstream input ("t.txt");//arquivo de input
	string aux;
	vector<string>links(0);//vector que conterá os links
	string args1;
	string args2;

	if (!input.is_open())//conferindo se o arquivo input foi aberto
	{
		printf("Erro ao abrir arquivo input\n");
		return 1;
	}

	printf("/////Lendo arquivo/////\n");
	while (getline(input,aux))
		links.push_back(aux);
	size_t found = links[0].find(" ");//pegando argumentos, se houver
	if(found!=string::npos)
	{
		args1=links[0][0];
		args2=links[0][2];
		links.erase(links.begin());
	}

	aux.clear();//limpando auxiliar

	printf("/////Baixando imagens/////\n");
	for (int i=0;i<links.size();i++)
	{
		aux="wkhtmltoimage \""+links[i]+"\" "+to_string(i)+".png";
		system((aux).c_str());//executa o comando acima (chama wkhtmltoimage)
	}
	aux.clear();

	printf("/////Chamando Agente/////\n");
	if(found!=string::npos)
		aux="java -jar Agente.jar "+to_string(links.size())+" "+args1+" "+args2;
	else
		aux="java -jar Agente.jar "+to_string(links.size());
	system((aux).c_str());

	for (int i=0;i<links.size();i++) //apagando imagens iniciais
		remove((to_string(i)+".png").c_str());

	printf("/////Completado com sucesso/////\n");
	return 0;
}
