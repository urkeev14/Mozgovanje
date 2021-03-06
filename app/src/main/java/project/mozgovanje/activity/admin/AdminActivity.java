package project.mozgovanje.activity.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import project.mozgovanje.R;
import project.mozgovanje.databinding.ActivityAdminBinding;
import project.mozgovanje.db.controller.RepositoryController;
import project.mozgovanje.model.question.Question;

public class AdminActivity extends AppCompatActivity implements AdminActivityRecyclerViewAdapter.OnItemManipulateListener {

    private ArrayList<Question> pendingQuestions;

    private ActivityAdminBinding binding;
    private AdminActivityRecyclerViewAdapter adapter;
//    private RefreshHandler refreshHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initQuestions();
        initBinding();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnLogout:
                RepositoryController.getInstance().logout(this);
                return true;
            default:
                break;
        }
        return false;
    }


    private void initQuestions() {
        pendingQuestions = RepositoryController.getInstance().getPendingQuestions();
    }

    private void initBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin);

        binding.activityAdminRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.activityAdminRecyclerView.setHasFixedSize(true);

        adapter = new AdminActivityRecyclerViewAdapter(this, pendingQuestions);
        adapter.setOnItemManipulateListener(this);
        binding.activityAdminRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.activityAdminSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshQuestions();
            }
        });
    }

    private void refreshQuestions() {
        RepositoryController.getInstance().refreshPendingQuestions();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pendingQuestions = new ArrayList<>();
                pendingQuestions = RepositoryController.getInstance().getPendingQuestions();
                adapter.setQuestions(pendingQuestions);
                adapter.notifyDataSetChanged();
                binding.activityAdminSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }


    @Override
    public void deleteQuestion(int position) {
        Question questionToDelete = adapter.getQuestion(position);
        RepositoryController.getInstance().deletePendingQuestion(questionToDelete); //POGLEDAJ LOGD
        adapter.removeQuestion(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void updateQuestion(int position) {
        Question questionToUpdate = adapter.getQuestion(position);
        RepositoryController.getInstance().updatePendingQuestion(questionToUpdate);
    }

    @Override
    public void createQuestion(int position) {
        Question selectedQuestion = adapter.getQuestion(position);
        RepositoryController.getInstance().createQuestion(this, selectedQuestion); //POGLEDAJ LOGD
        RepositoryController.getInstance().deletePendingQuestion(selectedQuestion);
        adapter.removeQuestion(position);
        adapter.notifyItemRemoved(position);
    }
}