###################################################
# A simple ruby script to push notifications to GCM
###################################################

# uses the gem from https://github.com/spacialdb/gcm
# gem install gcm
require 'gcm'

# an array of one or more client registration IDs
registration_ids= ["asdfasdfasdfa"]

API_SERVER_KEY = "ENTER_API_KEY_HERE"

gcm = GCM.new(API_SERVER_KEY)

options = { data: {score: Time.new.to_s}, collapse_key: "updated_score"}

response = gcm.send_notification(registration_ids, options)

# Run above script as follows
# ruby -rubygems notify_gcm.rb