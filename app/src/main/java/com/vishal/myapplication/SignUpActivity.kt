package com.vishal.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.vishal.socialmate.MainActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val txtReg = findViewById<TextView>(R.id.txt_switch1)

        txtReg.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        auth = FirebaseAuth.getInstance()

        val btn = findViewById<Button>(R.id.signup_btn)
        val signupName = findViewById<EditText>(R.id.signup_name)
        val signupEmail = findViewById<EditText>(R.id.signup_email)
        val signupPass = findViewById<EditText>(R.id.signup_pass)
        val signupConfirmPass = findViewById<EditText>(R.id.signup_cPass)

        val progressBar = findViewById<ProgressBar>(R.id.signup_progress)

        btn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val name = signupName.text.toString()
            val email = signupEmail.text.toString()
            val pass = signupPass.text.toString()
            val cPass = signupConfirmPass.text.toString()

            if (name.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Name can't be Empty \uD83D\uDE1F", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Email can't be Empty \uD83D\uDE1F", Toast.LENGTH_SHORT).show()
            } else if (pass.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Password can't be Empty \uD83D\uDE1F", Toast.LENGTH_SHORT).show()
            } else if (cPass.isEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Confirm password can't be Empty \uD83D\uDE1F", Toast.LENGTH_SHORT).show()
            } else if (!email.matches(emailPattern)) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Invalid Email Address \uD83E\uDD72", Toast.LENGTH_SHORT).show()
            } else if (pass != cPass){
                progressBar.visibility = View.GONE
                Toast.makeText(this,"Password and confirm password does not match \uD83E\uDEE2",Toast.LENGTH_SHORT).show()
            } else {
                progressBar.visibility = View.VISIBLE
                signupUser(email,pass,name)
            }

        }
    }

    private fun signupUser(email: String,password: String,name: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Storing data in Realtime database
                    val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
                    val userMap = mapOf(
                        "name" to name,
                        "email" to email
                    )
                    userId?.let { databaseReference.child(it).setValue(userMap) }

                    // Storing data in Cloud FireStore
                    val firestore = FirebaseFirestore.getInstance()
                    val usersCollection = firestore.collection("users")
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email
                    )
                    userId?.let { usersCollection.document(it).set(userData) }

                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                } else {
                    val exception = task.exception
                    Toast.makeText(this, "Sign up failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}