package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import models.MessageModel;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int SENDER_VIEWHOLDER = 0;
    private static final int RECEIVER_VIEWHOLDER = 1;

    private ArrayList<MessageModel> msgData;
    private Context context;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private MessageLongClickListener messageLongClickListener;

    public interface MessageLongClickListener {
        void onMessageLongClick(MessageModel message);
    }

    public MessageAdapter(ArrayList<MessageModel> msgData, Context context) {
        this.msgData = msgData;
        this.context = context;
    }

    public void setMessageLongClickListener(MessageLongClickListener listener) {
        this.messageLongClickListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == SENDER_VIEWHOLDER) {
            view = LayoutInflater.from(context).inflate(R.layout.sender_listitem, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.receiver_listitem, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = msgData.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return msgData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return msgData.get(position).getuId().equals(firebaseAuth.getUid()) ? SENDER_VIEWHOLDER : RECEIVER_VIEWHOLDER;
    }
    public void updateMessage(MessageModel oldMessage, MessageModel newMessage) {
        int index = msgData.indexOf(oldMessage);
        if (index != -1) {
            msgData.set(index, newMessage);
            notifyItemChanged(index);
        }
    }
    class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView messageText, messageTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && messageLongClickListener != null) {
                        MessageModel message = msgData.get(position);
                        messageLongClickListener.onMessageLongClick(message);
                        return true;
                    }
                    return false;
                }
            });
        }

        void bind(MessageModel message) {
            messageText.setText(message.getMsgText());
            long time = message.getMsgTime();
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            final String timeString = new SimpleDateFormat("HH:mm").format(cal.getTime());
            messageTime.setText(timeString);
        }
    }
}
