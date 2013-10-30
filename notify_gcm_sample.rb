require 'gcm'
API_SERVER_KEY = "ENTER_API_KEY_HERE"

gcm = GCM.new(API_SERVER_KEY)

registration_ids= ["asdfasdfasdfa"] # an array of one or more client registration IDs
options = {data: {score: Time.new.to_s}, collapse_key: "updated_score"}
response = gcm.send_notification(registration_ids, options)