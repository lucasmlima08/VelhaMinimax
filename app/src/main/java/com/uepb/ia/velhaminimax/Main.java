package com.uepb.ia.velhaminimax;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.Arrays;

public class Main extends Activity implements View.OnClickListener {

    LinearLayout p1,p2,p3;

    private String[] velhaAtual = {"?","?","?","?","?","?","?","?","?"};
    private Arvore raizAtual;
    private Arvore raizOriginal;
    private boolean terminou = false;

    private String strJogador = "O";
    private String strCPU = "X";
    private int vitoria = 1;
    private int derrota = -1;

    ImageButton[] botoes;
    Button bAux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        p1 = (LinearLayout) findViewById(R.id.p1);
        p2 = (LinearLayout) findViewById(R.id.p2);
        p3 = (LinearLayout) findViewById(R.id.p3);
        bAux = (Button) findViewById(R.id.bAux);

        criarBotoes();

        Audio.context = this;
        Audio.playBackgroundMusic();

        gerarArvoreThread("Aguarde", "Gerando a árvore. Isso vai demorar alguns segundos..", 33).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        reiniciarPartida();
        bAux.setText("PASSAR A VEZ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Audio.unpauseBackgroundMusic();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Audio.pauseBackgroundMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Audio.stopBackgroundMusic();
    }

    private void criarBotoes(){
        botoes = new ImageButton[9];
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
        param.weight = 1.0f;
        for (int i=0; i<botoes.length; i++){
            botoes[i] = new ImageButton(this);
            botoes[i].setId(i);
            botoes[i].setImageResource(R.drawable.v0);
            botoes[i].setBackgroundColor(0x00ffffff);
            botoes[i].setScaleType(ImageView.ScaleType.FIT_XY);
            botoes[i].setLayoutParams(param);
            botoes[i].setOnClickListener(this);
            if (i < 3)
                p1.addView(botoes[i]);
            else if (i < 6)
                p2.addView(botoes[i]);
            else
                p3.addView(botoes[i]);
        }
    }

    /** GERA A ÁRVORE INTEIRA E REALIZA BUSCA EM PROFUNDIDADE PARA SOMAR OS CUSTOS DE ESCOLHA */

    private Thread gerarArvoreThread(String titulo, String subtitulo, final int segundos){
        final ProgressDialog dialog = ProgressDialog.show(this,titulo,subtitulo);
        Thread thread = new Thread() {
            public void run() {
                try{
                    gerarArvore();
                    try {
                        Thread.sleep(segundos * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Log.e("Erro", e.getMessage());
                }
                dialog.dismiss();
            }
        };
        return thread;
    }

    private void gerarArvore(){
        Arvore arvore = new Arvore();
        arvore.criaFilhos();
        arvore = incluirCustoDeEscolha(arvore);
        raizOriginal = arvore;
        raizAtual = raizOriginal;
    }

    private Arvore incluirCustoDeEscolha(Arvore raiz){
        for (int i=0; i<raiz.getFilhos().size(); i++){
            incluirCustoDeEscolha(raiz.getFilhos().get(i));
            raiz.setCustoDeEscolha(raiz.getCustoDeEscolha()+raiz.getFilhos().get(i).getCustoDeEscolha());
        }
        return raiz;
    }

    /** MÉTODO DE JOGADA DO USUÁRIO */

    private void jogadaUsuario(int escolha){
        if (raizAtual.getFilhos().size() > 0 && !terminou){
            velhaAtual[escolha] = strJogador;
            // **********************************************************************************
            // Procura o nó raiz da jogada do usuário.
            for (int i=0; i<raizAtual.getFilhos().size(); i++)
                if (Arrays.equals(raizAtual.getFilhos().get(i).getVelha(), velhaAtual)){
                    raizAtual = raizAtual.getFilhos().get(i);
                    break;
                }
            // **********************************************************************************
            // Verifica se ganhou..
            if (raizAtual.getCustoDeEscolha() == derrota && raizAtual.getFilhos().size() == 0)
                terminou = true;
            else if (raizAtual.getCustoDeEscolha() == 0 && raizAtual.getFilhos().size() == 0)
                terminou = true;
        }
    }

    /** MÉTODO DE DECISÃO DA JOGADA DA CPU */

    private void jogadaCPU(){
        if (raizAtual.getFilhos().size() > 0 && !terminou){
            Arvore filhoVencedor = null, filhoSemDerrota = null, filhoDerrotado = null, maximizacao = null;
            boolean vencedor = false, perdedor = false;
            // **************************************************************************************
            // Percorre os filhos para encontrar a melhor jogada.
            int melhorCusto = raizAtual.getFilhos().get(0).getCustoDeEscolha();
            maximizacao = raizAtual.getFilhos().get(0);
            for (int i=0; i<raizAtual.getFilhos().size(); i++){
                Arvore filhoAux = raizAtual.getFilhos().get(i);
                // **********************************************************************************
                // Vai vencer..
                if ((filhoAux.getCustoDeEscolha() == vitoria)&&(filhoAux.getFilhos().size() == 0)){
                    vencedor = true;
                    filhoVencedor = filhoAux;
                    break;
                }
                // ***********************************************************************************
                // Procura o maior valor (maximização).
                // Se o usuário passar a vez, a maximização passa a ser minimização (por causa do nível da árvore).
                if (vitoria == 1){
                    if (raizAtual.getFilhos().get(i).getCustoDeEscolha() > melhorCusto){
                        melhorCusto = raizAtual.getFilhos().get(i).getCustoDeEscolha();
                        maximizacao = filhoAux;
                    }
                } else {
                    if (raizAtual.getFilhos().get(i).getCustoDeEscolha() < melhorCusto){
                        melhorCusto = raizAtual.getFilhos().get(i).getCustoDeEscolha();
                        maximizacao = filhoAux;
                    }
                }
                // *************************************************************************************
                // Verifica os filhos do filho atual, para saber se vai perder na próxima jogada do usuário.
                boolean perdedorAux = false;
                int nDerrotasAux = 0;
                for(int j=0; j<filhoAux.getFilhos().size(); j++){
                    if ((filhoAux.getFilhos().get(j).getCustoDeEscolha() == derrota)&&(filhoAux.getFilhos().get(j).getFilhos().size() == 0)){
                        perdedor = true;
                        perdedorAux = true;
                        nDerrotasAux++;
                    }
                }
                // Verifica o número de possíveis derrotas na próxima jogada.
                // Isso porque mesmo que seja derrotado ele irá escolher o caminho com o menor número de derrotas.
                if (nDerrotasAux < 2){
                    filhoDerrotado = filhoAux;
                }
                // Verifica se este é um filho onde não haverá derrotas na próxima jogada.
                if (!perdedorAux){
                    filhoSemDerrota = filhoAux;
                }
            }
            // ******************************************************************************************
            // Condições de escolha da jogada da CPU.
            if (vencedor)
                raizAtual = filhoVencedor;
            else if (perdedor && filhoSemDerrota != null)
                raizAtual = filhoSemDerrota;
            else if (perdedor && filhoSemDerrota == null)
                raizAtual = filhoDerrotado;
            else
                raizAtual = maximizacao;
            // ******************************************************************************************
            // Após descobrir o melhor filho, verifica em qual local a CPU vai jogar (na matriz).
            for (int i=0; i<raizAtual.getVelha().length; i++){
                if (!velhaAtual[i].equals(raizAtual.getVelha()[i])){
                    botoes[i].setImageResource(R.drawable.v1);
                    velhaAtual[i] = strCPU;
                    break;
                }
            }
            // ******************************************************************************************
            if (raizAtual.getCustoDeEscolha() == vitoria && raizAtual.getFilhos().size() == 0)
                terminou = true;
        }
    }

    /** MÉTODOS DE REINÍCIO DA PARTIDA */

    private void reiniciarPartida(){
        strJogador = "O";
        strCPU = "X";
        vitoria = 1;
        derrota = -1;
        raizAtual = raizOriginal;
        restaurarImagemDeBotoes();
        restaurarVelhaAtual();
        terminou = false;
    }

    private void restaurarVelhaAtual(){
        for (int i=0; i<velhaAtual.length; i++)
            velhaAtual[i] = "?";
    }

    private void restaurarImagemDeBotoes(){
        for (int i=0; i<botoes.length; i++)
            botoes[i].setImageResource(R.drawable.v0);
    }

    /** MÉTODOS DE EVENTOS */

    public void evento_bAux(View v){
        Audio.playSoundEffect("beep");
        if (bAux.getText().equals("PASSAR A VEZ")){
            strJogador = "X";
            strCPU = "O";
            vitoria = -1;
            derrota = 1;
            jogadaCPU();
            bAux.setText("TERMINAR");
        } else if (bAux.getText().equals("TERMINAR")){
            reiniciarPartida();
            bAux.setText("PASSAR A VEZ");
        }
    }

    public void onClick(View v) {
        Audio.playSoundEffect("beep");
        bAux.setText("TERMINAR");
        ImageButton b = (ImageButton) v;
        if (b.getDrawable().getConstantState().equals(getResources().getDrawable((R.drawable.v0)).getConstantState()) && !terminou){
            b.setImageResource(R.drawable.v2);
            jogadaUsuario(b.getId());
            jogadaCPU();
        }
    }
}
